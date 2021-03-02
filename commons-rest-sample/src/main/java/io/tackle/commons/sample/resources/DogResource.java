package io.tackle.commons.sample.resources;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.tackle.commons.sample.entities.Dog;

import java.util.List;

@ResourceProperties(hal = true)
public interface DogResource extends PanacheEntityResource<Dog, Long> {

    @MethodProperties(exposed = false)
    List<Dog> list(Page page, Sort sort);

}
