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
package io.tackle.commons.resources.hal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.quarkus.rest.data.panache.runtime.hal.HalEntityWrapper;
import io.quarkus.rest.data.panache.runtime.hal.HalLink;
import io.quarkus.rest.data.panache.runtime.hal.HalLinksProvider;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.IOException;
import java.util.Map;

@RegisterForReflection
public class HalCollectionEnrichedWrapperJacksonSerializer extends JsonSerializer<HalCollectionEnrichedWrapper> {

    private final HalLinksProvider linksExtractor;

    public HalCollectionEnrichedWrapperJacksonSerializer() {
        this.linksExtractor = new RestEasyHalLinksProvider();
    }

/*
    Commented as not used actually but in case of need just uncomment.

    HalCollectionEnrichedWrapperJacksonSerializer(HalLinksProvider linksExtractor) {
        this.linksExtractor = linksExtractor;
    }
*/

    @Override
    public void serialize(HalCollectionEnrichedWrapper wrapper, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        generator.writeStartObject();
        writeEmbedded(wrapper, generator, serializers);
        writeLinks(wrapper, generator);
        writeMetadata(wrapper, generator);
        generator.writeEndObject();
    }

    private void writeEmbedded(HalCollectionEnrichedWrapper wrapper, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        JsonSerializer<Object> entitySerializer = serializers.findValueSerializer(HalEntityWrapper.class);

        generator.writeFieldName("_embedded");
        generator.writeStartObject();
        generator.writeFieldName(wrapper.getCollectionName());
        generator.writeStartArray(wrapper.getCollection().size());
        for (Object entity : wrapper.getCollection()) {
            entitySerializer.serialize(new HalEntityWrapper(entity), generator, serializers);
        }
        generator.writeEndArray();
        generator.writeFieldName("_metadata");
        generator.writeObject(wrapper.getMetadata());
        generator.writeEndObject();
    }

    private void writeLinks(HalCollectionEnrichedWrapper wrapper, JsonGenerator generator) throws IOException {
        Map<String, HalLink> links = linksExtractor.getLinks(wrapper.getElementType());
        links.putAll(wrapper.getLinks());
        generator.writeFieldName("_links");
        generator.writeObject(links);
    }

    private void writeMetadata(HalCollectionEnrichedWrapper wrapper, JsonGenerator generator) throws IOException {
/*
        generator.writeFieldName("_metadata");
        generator.writeObject(wrapper.getMetadata());
*/
        generator.writeObjectField("total_count", wrapper.getTotalCount());
    }
}
