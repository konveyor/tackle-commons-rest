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
