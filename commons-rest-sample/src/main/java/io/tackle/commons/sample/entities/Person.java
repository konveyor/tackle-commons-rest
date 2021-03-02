package io.tackle.commons.sample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    public List<Dog> dogs = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        dogs.forEach(dog -> dog.owner = null);
    }
}
