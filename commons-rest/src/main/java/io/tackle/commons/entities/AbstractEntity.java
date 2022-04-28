/*
 * Copyright © 2021 Konveyor (https://konveyor.io/)
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
package io.tackle.commons.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.identity.SecurityIdentity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class AbstractEntity extends PanacheEntity {
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSx")
    @JsonIgnore
    @CreationTimestamp
    @Column(updatable=false)
    public Instant createTime;
    //    @JsonIgnore
    public String createUser;
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSx")
    @JsonIgnore
    @UpdateTimestamp
    public Instant updateTime;
    //    @JsonIgnore
    public String updateUser;
    @JsonIgnore
    public Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        final String username = getUsername();
        createUser = username;
        updateUser = username;
    }

    @PreUpdate
    protected void onUpdate() {
        updateUser = getUsername();
    }

    private String getUsername() {
        // based on the Quarkus issue
        // https://github.com/quarkusio/quarkus/issues/6948#issuecomment-619872942
        SecurityIdentity context = CDI.current().select(SecurityIdentity.class).get();
        if (Objects.nonNull(context)) {
            String username = context.getPrincipal().getName();
            if (Objects.nonNull(username)) {
                return username;
            }
        }
        // since all service are authenticated, it should never get here
        // so maybe worth evaluating to throw an exception? food for thoughts
        return "";
    }

}

