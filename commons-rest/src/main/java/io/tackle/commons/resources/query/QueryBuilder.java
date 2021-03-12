package io.tackle.commons.resources.query;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.resources.ListFilteredResource;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

// https://github.com/quarkusio/quarkus/issues/15088#issuecomment-783454416
// Class to cover the need of generating queries on our own
public class QueryBuilder<ENTITY extends PanacheEntity> implements QueryUriInfo<ENTITY>, QueryBuild<ENTITY> {

    public static final String DEFAULT_SQL_ROOT_TABLE_ALIAS = "table";
    private final Class<ENTITY> panacheEntity;
    private UriInfo uriInfo;
    private final Map<String, Boolean> filterableFields;

    QueryBuilder(Class<ENTITY> panacheEntity) {
        this.panacheEntity = panacheEntity;
        filterableFields = getFilterableFieldsMap(panacheEntity);
    }

    public static <ENTITY extends PanacheEntity> QueryUriInfo<ENTITY> withPanacheEntity(Class<ENTITY> panacheEntity) {
        return new QueryBuilder<>(panacheEntity);
    }

    public QueryBuild<ENTITY> andUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
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
        uriInfo.getQueryParameters(true).forEach((queryParamName, queryParamValues) -> {
            // replace this 'if' with 'filter' for streams?
            if (!(ListFilteredResource.QUERY_PARAM_SIZE.equals(queryParamName) ||
                    ListFilteredResource.QUERY_PARAM_PAGE.equals(queryParamName) ||
                    ListFilteredResource.QUERY_PARAM_SORT.equals(queryParamName))) {
                // if the filter name is not one of the fields annotated as Filterable
                // then the request is bad and the user should not repeat it without modifications
                // https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1
                if (!filterableFields.containsKey(queryParamName)) throw new WebApplicationException("Malformed syntax for a filter name", BAD_REQUEST);

                // build the FROM
                // the 'OneToMany' creates the expected "duplication" effect in the result set because of the relation's cardinality (1:M => 1 X M)
                // so it's added only if the queryParamName refers to a 'OneToMany' relationship
                fromBuilder.append(
                    Arrays.stream(panacheEntity.getDeclaredFields())
                        .filter(field -> isFieldToBeJoined(field, queryParamName))
                        .map(field -> String.format("JOIN %s.%s %s ", DEFAULT_SQL_ROOT_TABLE_ALIAS, field.getName(), getTableNameAlias(queryParamName)))
                        .collect(Collectors.joining())
                );

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
                    if (queryParamName.endsWith(".id")) {
                        whereBuilder.append(String.format("%s.%s = :%s",  getTableNameAlias(queryParamName), getPropertyName(queryParamName), randomParameterKey));
                        try {
                            queryParameters.put(randomParameterKey, Long.valueOf(value));
                        } catch (NumberFormatException e) {
                            throw new WebApplicationException("Malformed filter value", BAD_REQUEST);
                        }
                    } else {
                        whereBuilder.append(String.format("lower(%s.%s) LIKE lower(:%s)", getTableNameAlias(queryParamName), getPropertyName(queryParamName), randomParameterKey));
                        queryParameters.put(randomParameterKey, String.format("%%%s%%", value));
                    }
                    rawQueryParams.computeIfAbsent(queryParamName, k -> new ArrayList<>()).add(value);
                });
                whereBuilder.append(" )");
            }
        });
        String fromAndWhereQuery = fromBuilder.append(whereBuilder).toString();
        return Query.withQuery(String.format("SELECT %s %s", DEFAULT_SQL_ROOT_TABLE_ALIAS, fromAndWhereQuery)).andCountQuery(fromAndWhereQuery).andParameters(queryParameters).andRawQueryParams(rawQueryParams);
    }
    
    private String getTableNameAlias(String queryParamName) {
        return filterableFields.get(queryParamName) ?  
                queryParamName.substring(0, queryParamName.indexOf(".")) : 
                DEFAULT_SQL_ROOT_TABLE_ALIAS; 
    }

    private String getPropertyName(String queryParamName) {
        return filterableFields.get(queryParamName) ?  queryParamName.substring(queryParamName.indexOf(".") + 1) : queryParamName; 
    }

    /**
     * Method to have a Map with key the HQL field name and with value a Boolean true is the field refers
     * to a  {@link javax.persistence.OneToMany} relationship, false otherwise.
     * 
     * @param entityClass
     * @param <ENTITY>
     * @return
     */
    private static <ENTITY extends PanacheEntity> Map<String, Boolean> getFilterableFieldsMap(Class<ENTITY> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Filterable.class))
            .collect(
                Collectors.toMap(field -> {
                        final String value = field.getAnnotation(Filterable.class).filterName();
                        return value.isEmpty() ? field.getName() : value;
                    },
                    field -> field.isAnnotationPresent(OneToMany.class)));
    }

    // Method to evaluate if a specific class' field must be part of the join because to fulfill a filter coming
    // from a query parameter in the request
    private boolean isFieldToBeJoined(Field field, String queryParamName) {
        return field.isAnnotationPresent(OneToMany.class) &&
                field.isAnnotationPresent(Filterable.class) &&
                /**
                 * The 'isEmpty' check is not needed anymore because the {@link io.tackle.commons.annotations.processors.FilterableProcessor}
                 * prevents at build time having a {@link javax.persistence.OneToMany} annotated field
                 * without a valid {@link Filterable#filterName()} value
                 */
                // !field.getAnnotation(Filterable.class).filterName().isEmpty() &&
                field.getAnnotation(Filterable.class).filterName().equalsIgnoreCase(queryParamName);
    }
}
