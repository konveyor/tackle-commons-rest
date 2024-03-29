/*
 * Copyright © 2021 the Konveyor Contributors (https://konveyor.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.tackle.commons.sample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
    // this is wrong and FilterableProcessor must check this and prevent it from happening
    // @TODO
    @Filterable(filterName = "fishes.foo")
    public List<Fish> fishes = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "person_book",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @Filterable(filterName = "books.title")
    public List<Book> books = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        dogs.forEach(dog -> dog.owner = null);
        cats.forEach(cat -> cat.owner = null);
        horses.forEach(horse -> horse.owner = null);
        fishes.forEach(fish -> fish.owner = null);
    }
}
