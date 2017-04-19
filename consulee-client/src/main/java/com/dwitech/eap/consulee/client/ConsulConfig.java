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

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Holds the meta data for the registered service.
 */
public class ConsulConfig {
    private String serviceId;
    private String serviceName;
    private String serviceHost;
    private String servicePort;
    private String serviceTTL;
    private String serviceRoot; // TODO add serviceRoot as key/value

    private String consulHost;
    private String consulPort;


    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

	public String getServiceId() { return serviceId; }
	public void setServiceId(String serviceId) { this.serviceId = serviceId; }

	public String getServiceTTL() { return serviceTTL; }
	public void setServiceTTL(String serviceTTL) { this.serviceTTL = serviceTTL; }

	public String getServicePort() { return servicePort; }
	public void setServicePort(String servicePort) { this.servicePort = servicePort; }

    public String getServiceHost() { return serviceHost; }
    public void setServiceHost(String serviceHost) { this.serviceHost = serviceHost; }


    public String getConsulHost() {
        return consulHost;
    }
    public void setConsulHost(String consulHost) { this.consulHost = consulHost; }

    public String getConsulPort() {
        return consulPort;
    }
    public void setConsulPort(String consulPort) {
        this.consulPort = consulPort;
    }

    public String toJSON() {
        Writer w = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(w)) {
            generator.writeStartObject()
                    .write("serviceId", serviceId)
                    .write("serviceName", serviceName)
                    .write("serviceHost", serviceHost)
                    .write("servicePort", servicePort)
                    .write("serviceRoot", serviceRoot)
                    .write("serviceTTL", serviceTTL)
                    .write("consulHost", consulHost)
                    .write("consulPort", consulPort)
            .writeEnd();
        }
        return w.toString();
    }

    public String getServiceRoot() {
        return serviceRoot;
    }
    public void setServiceRoot(String serviceRoot) {
        this.serviceRoot = serviceRoot;
    }
}