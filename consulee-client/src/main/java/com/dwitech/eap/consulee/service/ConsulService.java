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
package com.dwitech.eap.consulee.service;

import com.dwitech.eap.consulee.model.DiscoveryResult;
import com.dwitech.eap.consulee.model.health.HealthCheck;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ConsulService {
    private static final String CONSUL_HEALTH_CHECK_API_ENDPOINT_TEMPLATE = "http://%s:%d/v1/health/service/%s?%s";
    private final String consulAgentLocalWebServiceHost;
    private final String consulAgentLocalWebServicePort;
    private String tag;

    public ConsulService(String consulHost, String consulPort, String tag) {
        this.consulAgentLocalWebServiceHost = consulHost;
        this.consulAgentLocalWebServicePort = consulPort;
        this.tag = tag;
    }

    public ConsulService(String consulHost, String consulPort) {
        this.consulAgentLocalWebServiceHost = consulHost;
        this.consulAgentLocalWebServicePort = consulPort;
    }

    public Set<DiscoveryResult> discoverHealthyNodes(Set<String> serviceNames) throws IOException {
        HashSet result = new HashSet();
        Iterator serviceNamesIt = serviceNames.iterator();

        while(serviceNamesIt.hasNext()) {
            String serviceName = (String)serviceNamesIt.next();
            String consulServiceHealthEndPoint = this.getConsulHealthCheckApiUrl(serviceName);
            String apiResponse = Utility.readUrl(consulServiceHealthEndPoint);
            HealthCheck[] healthChecks = new Gson().fromJson(apiResponse, HealthCheck[].class);
            Arrays.stream(healthChecks).forEach((healthCheck) -> {
                String ip = healthCheck.getService().getAddress();
                int port = healthCheck.getService().getPort().intValue();
                if(ip == null || ip.isEmpty()) {
                    ip = healthCheck.getNode().getAddress();
                }

                result.add(new DiscoveryResult(ip, port));
            });
        }

        return result;
    }

    private final String getConsulHealthCheckApiUrl(String serviceName) {
        StringBuffer queryParam = new StringBuffer("passing");
        if(this.tag != null) {
            queryParam.append("&tag=");
            queryParam.append(this.tag.trim());
        }

        return String.format(CONSUL_HEALTH_CHECK_API_ENDPOINT_TEMPLATE, this.consulAgentLocalWebServiceHost, Integer.valueOf(this.consulAgentLocalWebServicePort), serviceName, queryParam.toString());
    }
}
