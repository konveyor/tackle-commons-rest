package io.tackle.commons.resources.query;

import io.tackle.commons.entities.AbstractEntity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.Set;

@Entity
public class GoodEntity extends AbstractEntity {
    public String string;
    public Integer integer;
    @ElementCollection
    public Set<Long> longs;
}
