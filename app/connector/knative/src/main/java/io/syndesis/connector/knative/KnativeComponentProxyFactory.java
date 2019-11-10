/*
 * Copyright (C) 2016 Red Hat, Inc.
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
package io.syndesis.connector.knative;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.apache.commons.io.IOUtils;


public final class KnativeComponentProxyFactory implements ComponentProxyFactory {
    @Override
    public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
        return new ComponentProxyComponent(componentId, componentScheme, createCatalog()) {

            {
                setBeforeConsumer(exchange -> {
                    exchange.getMessage().removeHeader("CamelHttpUri");
                });
            }

            @Override
            protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) {
                // No specific component options
                return Optional.empty();
            }

            @Override
            protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme, Map<String, String> options) {
                return getCamelContext().getEndpoint(computeKnativeUri(scheme, options));
            }

        };
    }

    public static String computeKnativeUri(String scheme, Map<String, String> options) {
        Map<String, Object> uriOptions = new HashMap<>(options);
        String type = (String) uriOptions.remove("type");
        String name = (String) uriOptions.remove("name");
        String uri = scheme + "://" + type + "/" + name;
        if (!uriOptions.isEmpty()) {
            try {
                return URISupport.appendParametersToURI(uri, uriOptions);
            } catch (UnsupportedEncodingException | URISyntaxException e) {
                throw new IllegalStateException("Unable to append parameters to URI", e);
            }
        }
        return uri;
    }

    public static CamelCatalog createCatalog() {
        String jsonSchema;
        try (InputStream schemaStream = KnativeComponentProxyFactory.class.getResourceAsStream("/org/apache/camel/component/knative/knative.json")) {
            jsonSchema = IOUtils.toString(schemaStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        CamelCatalog catalog = new DefaultCamelCatalog(false);
        catalog.addComponent("knative", "org.apache.camel.component.knative.KnativeComponent", jsonSchema);

        return catalog;
    }
}
