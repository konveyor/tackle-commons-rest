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

import io.quarkus.rest.data.panache.runtime.hal.HalLink;
import io.quarkus.rest.data.panache.runtime.hal.HalLinksProvider;
import io.quarkus.rest.data.panache.runtime.resource.RESTEasyClassicResourceLinksProvider;
import io.quarkus.rest.data.panache.runtime.resource.ResourceLinksProvider;

import java.util.HashMap;
import java.util.Map;

final class RestEasyHalLinksProvider implements HalLinksProvider {

    private final ResourceLinksProvider linksProvider = new RESTEasyClassicResourceLinksProvider();

    @Override
    public Map<String, HalLink> getLinks(Class<?> entityClass) {
        return toHalLinkMap(linksProvider.getClassLinks(entityClass));
    }

    @Override
    public Map<String, HalLink> getLinks(Object entity) {
        return toHalLinkMap(linksProvider.getInstanceLinks(entity));
    }

    private Map<String, HalLink> toHalLinkMap(Map<String, String> links) {
        Map<String, HalLink> halLinks = new HashMap<>(links.size());
        for (Map.Entry<String, String> entry : links.entrySet()) {
            halLinks.put(entry.getKey(), new HalLink(entry.getValue()));
        }
        return halLinks;
    }
}
