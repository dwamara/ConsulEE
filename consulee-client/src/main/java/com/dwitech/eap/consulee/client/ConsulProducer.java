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

import com.dwitech.eap.consulee.ConsulConfigurationException;
import com.dwitech.eap.consulee.annotation.Consul;
import com.dwitech.eap.consulee.client.ConsulServiceClient.Builder;
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

    private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoopee");

    private Map<String, Object> consuleeConfig = Collections.EMPTY_MAP;

    /**
     * Creates a ConsulServiceClient for the named service.
     *
     * @param ip The injection point
     * @return a configured Consul service client
     */
    @Consul
    @Produces
    @Dependent
    public ConsulServiceClient lookup(InjectionPoint ip) {

        final String applicationName = ip.getAnnotated().getAnnotation(Consul.class).serviceName();

        LOGGER.config(() -> "producing " + applicationName);

        String serviceUrl = "http://" + readProperty("consuleeService", consuleeConfig);
        LOGGER.config(() -> "Service URL: " + serviceUrl);

        return new Builder(applicationName).serviceUrl(serviceUrl).build();
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

            consuleeConfig = (Map<String, Object>) props.get("consul");

            if (!isConsulEnabled()) {
                setServiceName(readProperty("serviceName", consuleeConfig));
            }

        } catch (YAMLException e) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }
    }
}
