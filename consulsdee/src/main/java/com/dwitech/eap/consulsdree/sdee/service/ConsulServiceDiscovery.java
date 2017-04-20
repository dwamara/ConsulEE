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
package com.dwitech.eap.consulsdree.sdee.service;

import com.dwitech.eap.consulsdree.sdee.ConsulConfigurationException;
import com.dwitech.eap.consulsdree.sdee.client.ConsulConfig;
import com.dwitech.eap.consulsdree.sdee.client.ConsulServiceUnavailableException;
import com.dwitech.eap.consulsdree.sdee.model.DiscoveryResult;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Optional.ofNullable;
import static java.util.logging.Logger.getLogger;

/**
 *
 */
public class ConsulServiceDiscovery {
    private static final Logger LOGGER = getLogger(ConsulServiceDiscovery.class.getName());

    private final String applicationName;

    public ConsulServiceDiscovery(final String applicationName) {
        this.applicationName = applicationName;
    }

    public ConsulConfig discoverServiceConfiguration() {
        try {
            final Set consulServiceNames = new HashSet();
            consulServiceNames.add(applicationName);

            final ConsulConfig consulConfig = readConfiguration();

            Set consulDiscoveryResults = (new ConsulService(consulConfig.getConsulHost(), consulConfig.getConsulPort())).discoverHealthyNodes(consulServiceNames);
            Iterator consulDiscoveryResultsIt = consulDiscoveryResults.iterator();
            while (consulDiscoveryResultsIt.hasNext()) {
                DiscoveryResult discoveryResult = (DiscoveryResult)consulDiscoveryResultsIt.next();
                consulConfig.setServiceHost(discoveryResult.getIp());
                consulConfig.setServicePort(String.valueOf(discoveryResult.getPort()));
                break;
            }
            return consulConfig;
        } catch (IOException ioExc) {
            throw new ConsulServiceUnavailableException(ioExc);
        }
    }

    private ConsulConfig readConfiguration() throws ConsulConfigurationException {
        final ConsulConfig consulConfiguration = new ConsulConfig();
        Map<String, Object> consulConfig = EMPTY_MAP;
        try {
            final Yaml yaml = new Yaml();
            final Map<String, Object> props = (Map<String, Object>) yaml.load(currentThread().getContextClassLoader().getResourceAsStream("/consul.yml"));
            consulConfig = (Map<String, Object>) props.get("consul");
        } catch (YAMLException yExc) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }

        consulConfiguration.setConsulHost(readProperty("consulHost", consulConfig));
        consulConfiguration.setConsulPort(readProperty("consulPort", consulConfig));

        LOGGER.config(() -> "application config for consul: " + consulConfiguration.toJSON());

        return consulConfiguration;
    }

    private String readProperty(final String key, Map<String, Object> consulConfig) {
        String property = ofNullable(getProperty(key))
                .orElseGet(() -> {
                    String envProp = ofNullable(System.getenv(applicationName + "." + key))
                            .orElseGet(() -> {
                                String confProp = ofNullable(consulConfig.get(key))
                                        .orElseThrow(() -> new ConsulConfigurationException(key + " must be configured either in consul.yml or as env parameter"))
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });
        return property;
    }
}