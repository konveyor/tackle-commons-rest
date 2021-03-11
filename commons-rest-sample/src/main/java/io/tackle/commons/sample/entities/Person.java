package io.tackle.commons.sample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import java.util.ArrayList;
import java.util.List;

@Entity
@SQLDelete(sql = "UPDATE person SET deleted = true WHERE id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "deleted = false")
public class Person extends AbstractEntity {
    public String name;
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonBackReference
    @Filterable(filterName = "cani.name")
    public List<Dog> dogs = new ArrayList<>();
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonBackReference("catsReference")
    @Filterable(filterName = "cats.name")
    public List<Cat> cats = new ArrayList<>();
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonBackReference("horsesReference")
    public List<Horse> horses = new ArrayList<>();
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonBackReference("fishesReference")
    // this should not be allowed really because with a referenced entity
    // it's impossible to know which field to use (or could we apply 'id' by default?)
    @Filterable
    public List<Fish> fishes = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        dogs.forEach(dog -> dog.owner = null);
        cats.forEach(cat -> cat.owner = null);
        horses.forEach(horse -> horse.owner = null);
        fishes.forEach(fish -> fish.owner = null);
    }
}
