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

import com.dwitech.eap.consulee.ConsulEEConfigurationException;
import com.dwitech.eap.consulee.ConsulEEExtensionHelper;
import com.dwitech.eap.consulee.annotation.ConsulEE;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * CDI Producer for ConsulEEServiceClient.
 *
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
@ApplicationScoped
public class ConsulEEProducer {

    private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoopee");

    private Map<String, Object> consuleeConfig = Collections.EMPTY_MAP;

    /**
     * Creates a ConsulEEServiceClient for the named service.
     *
     * @param ip The injection point
     * @return a configured ConsulEE service client
     */
    @ConsulEE
    @Produces
    @Dependent
    public ConsulEEServiceClient lookup(InjectionPoint ip) {

        final String applicationName = ip.getAnnotated().getAnnotation(ConsulEE.class).serviceName();

        LOGGER.config(() -> "producing " + applicationName);

        String serviceUrl = "http://" + readProperty("consuleeService", consuleeConfig);
        LOGGER.config(() -> "Service URL: " + serviceUrl);

        return new ConsulEEServiceClient.Builder(applicationName)
                .serviceUrl(serviceUrl)
                .build();
    }

    private String readProperty(final String key, Map<String, Object> snoopConfig) {
        String property = Optional.ofNullable(System.getProperty(key))
                .orElseGet(() -> {
                    String envProp = Optional.ofNullable(System.getenv(key))
                            .orElseGet(() -> {
                                String confProp = Optional.ofNullable(snoopConfig.get(key))
                                        .orElseThrow(() -> {
                                            return new ConsulEEConfigurationException(key + " must be configured either in consulee.yml or as env or system property");
                                        })
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });

        return property;
    }

    /**
     * Initializes the producer with the ConsulEE configuration properties.
     */
    @PostConstruct
    private void init() {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> props = (Map<String, Object>) yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("/consulee.yml"));

            consuleeConfig = (Map<String, Object>) props.get("consulee");

            if (!ConsulEEExtensionHelper.isConsulEnabled()) {
                ConsulEEExtensionHelper.setServiceName(readProperty("serviceName", consuleeConfig));
            }

        } catch (YAMLException e) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }
    }
}
