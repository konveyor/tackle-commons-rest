package io.tackle.commons.sample.resources;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.tackle.commons.sample.entities.Person;

@ResourceProperties(hal = true)
public interface PersonResource extends PanacheEntityResource<Person, Long> {}
