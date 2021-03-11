package io.tackle.commons.resources.query;

import java.util.List;
import java.util.Map;

public class Query {
    private String query;
    private String countQuery;
    private Map<String, Object> queryParameters;
    private Map<String, List<String>> rawQueryParams;

    public static Query withQuery(String query) {
        Query filter = new Query();
        filter.query = query;
        return filter;
    }

    public Query andCountQuery(String countQuery) {
        this.countQuery = countQuery;
        return this;
    }

    public Query andParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public Query andRawQueryParams(Map<String, List<String>> rawQueryParams) {
        this.rawQueryParams = rawQueryParams;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public String getCountQuery() {
        return countQuery;
    }

    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, List<String>> getRawQueryParams() {
        return rawQueryParams;
    }
}
