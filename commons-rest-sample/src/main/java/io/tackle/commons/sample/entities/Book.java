package io.tackle.commons.sample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Book extends AbstractEntity {
    @Filterable
    public String title;
    public Double price;
    @ManyToMany(mappedBy = "books")
    @JsonBackReference
    public List<Person> readers = new ArrayList<>();
}
