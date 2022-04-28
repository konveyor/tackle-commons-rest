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
package io.tackle.commons.resources.hal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.tackle.commons.resources.responses.Metadata;
import io.quarkus.rest.data.panache.runtime.hal.HalCollectionWrapper;

import java.util.Collection;

@JsonSerialize(using = HalCollectionEnrichedWrapperJacksonSerializer.class)
public class HalCollectionEnrichedWrapper extends HalCollectionWrapper {

    /**
     * TODO Decide which one to keep: metadata object or straight totalCount field?
     * I prefer metadata object because it's inside the '_embedded' component of the
     * HAL response and it contains the metadata about the embedded collection
     * referenced by the name of resources type with the collection.
     */
    private final Metadata metadata;
    private final long totalCount;

    public HalCollectionEnrichedWrapper(Collection<Object> collection, Class<?> elementType, String collectionName, long totalCount) {
        super(collection, elementType, collectionName);
        metadata = Metadata.withTotalCount(totalCount);
        this.totalCount = totalCount;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public long getTotalCount() {
        return totalCount;
    }

}
