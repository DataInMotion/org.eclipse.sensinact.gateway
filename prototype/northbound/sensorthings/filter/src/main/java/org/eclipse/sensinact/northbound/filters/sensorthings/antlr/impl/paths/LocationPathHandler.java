/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

/**
 * @author thoma
 *
 */
public class LocationPathHandler {

    private final ProviderSnapshot provider;
    private final List<ResourceSnapshot> resources;

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("things", this::subThings,
            "historicallocations", this::subHistoricalLocations);

    public LocationPathHandler(final ProviderSnapshot provider, final List<ResourceSnapshot> resources) {
        this.provider = provider;
        this.resources = resources;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        if (parts.length == 1) {
            switch (parts[0]) {
            case "id":
                // Provider
                return provider.getName();

            default:
                return PathUtils.getProviderLevelField(provider, resources, parts[0]);
            }
        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    private Object subThings(final String path) {
        return new ThingPathHandler(provider, resources);
    }

    private Object subHistoricalLocations(final String path) {
        return new HistoricalLocationPathHandler(provider, resources).handle(path);
    }
}
