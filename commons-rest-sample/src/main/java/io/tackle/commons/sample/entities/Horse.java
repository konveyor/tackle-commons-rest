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

import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@SQLDelete(sql = "UPDATE horse SET deleted = true WHERE id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "deleted = false")
public class Horse extends AbstractEntity {
    @Column(unique=true)
    @Filterable
    public String name;
    public String color;
    @ManyToOne
    @Filterable(filterName = "owner.name")
    public Person owner;
    @ManyToOne
    @Filterable(filterName = "breed.id")
    public Breed breed;
}
