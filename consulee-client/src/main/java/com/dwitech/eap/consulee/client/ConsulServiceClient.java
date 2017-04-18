/*
 * The MIT License
 *
 * Copyright 2015 Ivar Grimstad (ivar.grimstad@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dwitech.eap.consulee.client;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Client API for calling services registered with Consul.
 */
public class ConsulServiceClient {

    private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoopee");
    private static final String DEFAULT_BASE_URI = "http://localhost:8080/snoopee-service/";

    private final String applicationName;
    private final String serviceUrl;

    static final class Builder {
        private final String applicationName;
        private String serviceUrl = DEFAULT_BASE_URI;

        Builder(final String applicationName) {
            this.applicationName = applicationName;
        }

        Builder serviceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
            return this;
        }

        ConsulServiceClient build() {
            return new ConsulServiceClient(this);
        }
    }

    private ConsulServiceClient(final Builder builder) {
        this.applicationName = builder.applicationName;
        this.serviceUrl = builder.serviceUrl;
        LOGGER.info(() -> "client created for " + applicationName);
    }

    /**
     * Locator to get the service root for the service registered with Consul.
     *
     * Use this method if the convenience methods simpleXXX are not sufficient or to avoid the extra call to Consul for
     * every request.
     *
     * @return the serviceRoot
     *
     * @throws ConsulServiceUnavailableException if service is not available
     */
    public WebTarget getServiceRoot() throws ConsulServiceUnavailableException {
        ConsulConfig consulConfig = getConfigFromConsul();
        LOGGER.fine(() -> "looking up service for " + applicationName);

        return newClient().target(consulConfig.getServiceHome()).path(consulConfig.getServiceRoot());
    }

    private ConsulConfig getConfigFromConsul() throws ConsulServiceUnavailableException {
        try {
            Response response = newClient()
                    .target(serviceUrl)
                    .path("api")
                    .path("services")
                    .path(applicationName)
                    .request(APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                return response.readEntity(ConsulConfig.class);
            } else {
                throw new ConsulServiceUnavailableException("Response from \"" + serviceUrl + "\"=" + response.getStatus());
            }
        } catch (ProcessingException e) {
            throw new ConsulServiceUnavailableException(e);
        }
    }
}