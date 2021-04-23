package io.tackle.commons.resources.query;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public interface QueryParameters<ENTITY extends PanacheEntity> {
    QueryBuild<ENTITY> andUriInfo(UriInfo uriInfo);
    QueryBuild<ENTITY> andMultivaluedMap(MultivaluedMap<String, String> multivaluedMap);
}
