package io.tackle.commons.resources;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public interface TypedWebMethod<E extends PanacheEntity> {
    Class<E> getPanacheEntityType();
}
