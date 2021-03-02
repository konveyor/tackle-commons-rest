package io.tackle.commons.sample.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@SQLDelete(sql = "UPDATE dog SET deleted = true WHERE id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "deleted = false")
public class Dog extends AbstractEntity {
    @Column(unique=true)
    @Filterable
    public String name;
    public String race;
    @ManyToOne
    @Filterable(filterName = "owner.name")
    public Person owner;
}
