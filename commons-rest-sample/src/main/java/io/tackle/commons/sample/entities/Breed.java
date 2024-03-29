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
import io.tackle.commons.annotations.CheckType;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@SQLDelete(sql = "UPDATE breed SET deleted = true WHERE id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "deleted = false")
public class Breed extends AbstractEntity {
    @Filterable
    public String name;
    @Filterable(check = CheckType.EQUAL)
    public String origin;
    @Filterable(check = CheckType.EQUAL)
    public Long internationalId;
    @ElementCollection
    @Filterable(check = CheckType.EQUAL, filterName = "translations.translations")
    public Set<String> translations = new HashSet<>();
    @OneToMany(mappedBy = "breed", fetch = FetchType.LAZY)
    @JsonBackReference
    @Filterable(filterName = "dogs.name")
    public List<Dog> dogs = new ArrayList<>();

    @PreRemove
    private void preRemove() {
        dogs.forEach(dog -> dog.owner = null);
    }
}
