/*
 * Copyright © 2021 Konveyor (https://konveyor.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.tackle.commons.resources.query;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.tackle.commons.annotations.CheckType;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.resources.ListFilteredResource;
import org.jboss.logging.Logger;

import javax.persistence.ElementCollection;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

// https://github.com/quarkusio/quarkus/issues/15088#issuecomment-783454416
// Class to cover the need of generating queries on our own
public class QueryBuilder<ENTITY extends PanacheEntity> implements QueryParameters<ENTITY>, QueryBuild<ENTITY> {

    private static final Logger LOGGER = Logger.getLogger(QueryBuilder.class);
    public static final String DEFAULT_SQL_ROOT_TABLE_ALIAS = "table";
    // this value could be made dynamically read from PanacheEntity
    // searching for the name of the field with annotation @Id.
    // But for the time being it's a good enough solution without adding further reflection here
    private static final String ID_FIELD_NAME_FROM_PANACHE_ENTITY = "id";
    private static final QueryParameterGenerators EQUAL_QUERY_PARAMETER_GENERATORS = new QueryParameterGenerators();
    private final Class<ENTITY> panacheEntity;
    private MultivaluedMap<String, String> multivaluedMap;
    private final Map<String, Field> filterableFields;

    static {
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Long.class, Long::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(String.class, Function.identity());
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Integer.class, Integer::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Byte.class, Byte::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Short.class, Short::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Float.class, Float::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Double.class, Double::valueOf);
        EQUAL_QUERY_PARAMETER_GENERATORS.putQueryParameterGenerator(Boolean.class, Boolean::valueOf);
    }

    QueryBuilder(Class<ENTITY> panacheEntity) {
        this.panacheEntity = panacheEntity;
        filterableFields = getFilterableFieldsMap(panacheEntity);
    }

    public static <ENTITY extends PanacheEntity> QueryParameters<ENTITY> withPanacheEntity(Class<ENTITY> panacheEntity) {
        return new QueryBuilder<>(panacheEntity);
    }

    public QueryBuild<ENTITY> andUriInfo(UriInfo uriInfo) {
        this.multivaluedMap = uriInfo.getQueryParameters(true);
        return this;
    }

    public QueryBuild<ENTITY> andMultivaluedMap(MultivaluedMap<String, String> multivaluedMap) {
        this.multivaluedMap = multivaluedMap;
        return this;
    }

    public Query build() {
        final StringBuilder fromBuilder = new StringBuilder(String.format("FROM %s %s ", panacheEntity.getSimpleName(), DEFAULT_SQL_ROOT_TABLE_ALIAS));
        fromBuilder.append(
            Arrays.stream(panacheEntity.getDeclaredFields())
                .filter(field ->
                    // 'ManyToOne' has been tested and it's something we must have
                    (field.isAnnotationPresent(ManyToOne.class) ||
                    // 'OneToOne' has just been added for sake of having it done
                    // without a real use case yet so it can be discussed later
                    field.isAnnotationPresent(OneToOne.class)))
                .map(field -> String.format("LEFT JOIN %s.%s ", DEFAULT_SQL_ROOT_TABLE_ALIAS, field.getName()))
                .collect(Collectors.joining())
        );
        final StringBuilder whereBuilder = new StringBuilder();
        final Map<String, Object> queryParameters = new HashMap<>();
        final Map<String, List<String>> rawQueryParams = new HashMap<>();
        final AtomicBoolean distinctRequired = new AtomicBoolean(false);
        multivaluedMap.forEach((queryParamName, queryParamValues) -> {
            // replace this 'if' with 'filter' for streams?
            if (!(ListFilteredResource.QUERY_PARAM_SIZE.equals(queryParamName) ||
                    ListFilteredResource.QUERY_PARAM_PAGE.equals(queryParamName) ||
                    ListFilteredResource.QUERY_PARAM_SORT.equals(queryParamName))) {
                // if the filter name is not one of the fields annotated as Filterable
                // then the request is bad and the user should not repeat it without modifications
                // https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1
                if (!filterableFields.containsKey(queryParamName)) throw new BadRequestException("Malformed syntax for a filter name");

                // build the FROM
                // the 'OneToMany' creates the expected "duplication" effect in the result set because of the relation's cardinality (1:M => 1 X M)
                // so it's added only if the queryParamName refers to a 'OneToMany' relationship
                Field field = filterableFields.get(queryParamName);
                if (isToManyAssociation(field)) {
                    fromBuilder.append(String.format("JOIN %s.%s %s ", DEFAULT_SQL_ROOT_TABLE_ALIAS, field.getName(), getTableNameAlias(queryParamName)));
                    distinctRequired.compareAndSet(false, true);
                }

                // build the WHERE
                if (whereBuilder.length() != 0) whereBuilder.append(" and ");
                else whereBuilder.append("WHERE ");
                whereBuilder.append("( ");
                AtomicBoolean isFirst = new AtomicBoolean(true);
                queryParamValues.forEach(value -> {
                    if (!isFirst.compareAndSet(true, false)) whereBuilder.append(" or ");
                    String randomParameterKey = queryParamName.replace('.', '_') + ThreadLocalRandom.current().nextInt(0, 1001);
                    // https://github.com/quarkusio/quarkus/issues/15088#issuecomment-783454416
                    // Due to the need of generating queries on our own, where parameters must have a prefix
                    //
                    // filter works with a `LIKE` case insensitive approach for every param but the 'id' which requires a check for equality
                    if (queryParamName.endsWith(".id") || ID_FIELD_NAME_FROM_PANACHE_ENTITY.equals(queryParamName)) {
                        whereBuilder.append(String.format("%s%s = :%s",  getWhereTableNameAlias(queryParamName), getPropertyName(queryParamName), randomParameterKey));
                        // this is based on the assumption that the entity has a Long id, i.e. it's a PanacheEntity
                        queryParameters.put(randomParameterKey, EQUAL_QUERY_PARAMETER_GENERATORS.getQueryParameterValue(Long.class, value));
                    } else if (CheckType.EQUAL.equals(field.getAnnotation(Filterable.class).check())){
                        whereBuilder.append(String.format("%s%s = :%s", getWhereTableNameAlias(queryParamName), getPropertyName(queryParamName), randomParameterKey));
                        queryParameters.put(randomParameterKey, EQUAL_QUERY_PARAMETER_GENERATORS.getQueryParameterValue(getType(field), value));
                    } else if (CheckType.LIKE.equals(field.getAnnotation(Filterable.class).check())) {
                        whereBuilder.append(String.format("lower(%s%s) LIKE lower(:%s)", getWhereTableNameAlias(queryParamName), getPropertyName(queryParamName), randomParameterKey));
                        queryParameters.put(randomParameterKey, String.format("%%%s%%", value));
                    }
                    rawQueryParams.computeIfAbsent(queryParamName, k -> new ArrayList<>()).add(value);
                });
                whereBuilder.append(" )");
            }
        });
        String fromAndWhereQuery = fromBuilder.append(whereBuilder).toString();
        // 'select distinct' can not be used for the general purpose query
        // because when sorting by a referenced collection's size (using the '.size()' function)
        // the sorting creates a 'order by (select count(foo.id))' like query that generates
        // "org.postgresql.util.PSQLException: ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list"
        // At the same time, trying to add the "count(foo.id) as foo_count" part in the select distinct will cause
        // a failure for Hibernate because the "foo_count" field can not be mapped into an entity's field.
        return Query.withQuery(String.format("SELECT %s %s", DEFAULT_SQL_ROOT_TABLE_ALIAS, fromAndWhereQuery))
                .andCountQuery(String.format("SELECT %s %s %s", distinctRequired.get() ? "distinct":"", DEFAULT_SQL_ROOT_TABLE_ALIAS, fromAndWhereQuery))
                .andParameters(queryParameters)
                .andRawQueryParams(rawQueryParams);
    }

    private String getTableNameAlias(String queryParamName) {
        if (filterableFields.get(queryParamName).isAnnotationPresent(ElementCollection.class)) return "";
        else if (isToManyAssociation(filterableFields.get(queryParamName))) return queryParamName.substring(0, queryParamName.indexOf("."));
        else return DEFAULT_SQL_ROOT_TABLE_ALIAS;
    }

    private String getWhereTableNameAlias(String queryParamName) {
        final String tableNameAlias = getTableNameAlias(queryParamName);
        return tableNameAlias.isEmpty() ? tableNameAlias : tableNameAlias + ".";
    }

    private String getPropertyName(String queryParamName) {
        return isToManyAssociation(filterableFields.get(queryParamName)) ?  queryParamName.substring(queryParamName.indexOf(".") + 1) : queryParamName;
    }

    /**
     * Method to have a Map with key the HQL field name and with value a Boolean true is the field refers
     * to a  {@link javax.persistence.OneToMany} relationship, false otherwise.
     *
     * @param <ENTITY>
     * @param entityClass
     * @return
     */
    private static <ENTITY extends PanacheEntity> Map<String, Field> getFilterableFieldsMap(Class<ENTITY> entityClass) {
        final Map<String, Field> filterableFieldsMap = Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Filterable.class))
            .collect(
                Collectors.toMap(field -> {
                        final String value = field.getAnnotation(Filterable.class).filterName();
                        return value.isEmpty() ? field.getName() : value;
                    },
                    field -> field));
        /**
         * based on <ENTITY extends PanacheEntity> above, the {@link PanacheEntity.id} field is always available
         * and it's considered a field the user can always filter by
         */
        try {
            filterableFieldsMap.put(ID_FIELD_NAME_FROM_PANACHE_ENTITY,  entityClass.getField(ID_FIELD_NAME_FROM_PANACHE_ENTITY));
        } catch (NoSuchFieldException e) {
            LOGGER.warnf("Filtering by @Id field won't be available because %s has not %s field.", entityClass.getName(), ID_FIELD_NAME_FROM_PANACHE_ENTITY);
        }
        return filterableFieldsMap;
    }

    private static boolean isToManyAssociation(Field field) {
        return field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(ElementCollection.class);
    }

    /**
     * @param field
     * @return
     * Long foo -> returns Long.class
     * Set<String> foo -> return String.class
     */
    protected static Class<?> getType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof Class) return (Class<?>) type;
        else if (type instanceof ParameterizedType) {
            Type genericTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
            return (Class<?>) genericTypeArgument;
        } else {
            throw new BadRequestException("Not supported filter");
        }
    }
}
