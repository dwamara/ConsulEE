/*
 * Copyright 2017 Daniel Wamara (dwamara@dwitech.com)
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
package com.dwitech.eap.consulee.client;

import com.dwitech.eap.consulee.ConsulConfigurationException;
import com.dwitech.eap.consulee.annotation.Consul;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.dwitech.eap.consulee.ConsulExtensionHelper.isConsulEnabled;
import static com.dwitech.eap.consulee.ConsulExtensionHelper.setServiceName;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

/**
 * CDI Producer for ConsulServiceClient.
 */
@ApplicationScoped
public class ConsulProducer {
    private static final Logger LOGGER = Logger.getLogger("com.dwitech.eap.consulee");
    private Map<String, Object> consulConfig = Collections.EMPTY_MAP;

    /**
     * Creates a ConsulServiceClient for the named service.
     * @param ip The injection point
     * @return a configured Consul service client
     */
    @Consul @Produces @Dependent
    public ConsulServiceClient lookup(InjectionPoint ip) {
        final String applicationName = ip.getAnnotated().getAnnotation(Consul.class).serviceName();
        LOGGER.config(() -> "producing " + applicationName);
        return new ConsulServiceClient(applicationName);
    }

    private String readProperty(final String key, Map<String, Object> snoopConfig) {
        String property = ofNullable(System.getProperty(key))
                .orElseGet(() -> {
                    String envProp = ofNullable(System.getenv(key))
                            .orElseGet(() -> {
                                String confProp = ofNullable(snoopConfig.get(key))
                                        .orElseThrow(() -> new ConsulConfigurationException(key + " must be configured either in consul.yml or as env or system property"))
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });
        return property;
    }

    /**
     * Initializes the producer with the Consul configuration properties.
     */
    @PostConstruct
    private void init() {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> props = (Map<String, Object>) yaml.load(currentThread().getContextClassLoader().getResourceAsStream("/consul.yml"));
            consulConfig = (Map<String, Object>) props.get("consul");

            if (!isConsulEnabled()) {
                setServiceName(readProperty("serviceName", consulConfig));
            }
        } catch (YAMLException e) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }
    }
}