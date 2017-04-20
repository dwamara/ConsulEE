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
package com.dwitech.eap.consulsdree.sdee.client;

import com.dwitech.eap.consulsdree.sdee.service.ConsulServiceDiscovery;

import javax.ws.rs.client.WebTarget;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static javax.ws.rs.client.ClientBuilder.newClient;

/**
 * Client API for calling services registered with Consul.
 */
public class ConsulServiceClient {
    private static final Logger LOGGER = getLogger(ConsulServiceClient.class.getName());
    private final String applicationName;

    public ConsulServiceClient(final String applicationName) {
        this.applicationName = applicationName;
        LOGGER.info(() -> "client created for " + applicationName);
    }

    /**
     * Locator to get the service root for the service registered with Consul.
     * @return the serviceRoot
     * @throws ConsulServiceUnavailableException if service is not available
     */
    public WebTarget getServiceRoot() throws ConsulServiceUnavailableException {
        ConsulConfig consulConfig = new ConsulServiceDiscovery(applicationName).discoverServiceConfiguration();
        LOGGER.fine(() -> "looking up service for " + applicationName);
        return newClient().target("http://" + consulConfig.getServiceHost() + ":" + consulConfig.getServicePort());
    }
}