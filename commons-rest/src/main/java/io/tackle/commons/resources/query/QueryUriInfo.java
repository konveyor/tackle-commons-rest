package io.tackle.commons.resources.query;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.ws.rs.core.UriInfo;

public interface QueryUriInfo<ENTITY extends PanacheEntity> {
    QueryBuild<ENTITY> andUriInfo(UriInfo uriInfo);
}
