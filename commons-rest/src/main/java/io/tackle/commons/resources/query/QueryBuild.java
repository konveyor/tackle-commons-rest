package io.tackle.commons.resources.query;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public interface QueryBuild<ENTITY extends PanacheEntity> {
    Query build();
}
