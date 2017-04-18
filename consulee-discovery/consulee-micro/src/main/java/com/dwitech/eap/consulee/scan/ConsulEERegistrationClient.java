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
package com.dwitech.eap.consulee.scan;

import com.dwitech.eap.consulee.ConsulEEConfigurationException;
import com.dwitech.eap.consulee.annotation.ConsulEEClient;
import com.dwitech.eap.consulee.client.ConsulEEConfig;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Registers with ConsulEE and gives heartbeats every 10 second.
 *
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
@ConsulEEClient
public class ConsulEERegistrationClient {

    private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoopee");

    private String serviceUrl;
    private final ConsulEEConfig applicationConfig = new ConsulEEConfig();

    private Timer timer;

    @Inject
    private Event<ConsulEEConfig> configuredEvent;

    private void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOGGER.config("Checking if ConsulEE is enabled");

        if (ConsulEEExtensionHelper.isSnoopEnabled()) {

            Client client = ClientBuilder.newClient();

            try {
                readConfiguration();
                LOGGER.config(() -> "Registering " + applicationConfig.getServiceName());

                Response response = client
                        .target(serviceUrl)
                        .path("api")
                        .path("services")
                        .request()
                        .post(Entity.entity(applicationConfig, APPLICATION_JSON));

                LOGGER.config(() -> "Fire health event");
                configuredEvent.fire(applicationConfig);

            } catch (ConsulEEConfigurationException e) {
                LOGGER.severe(() -> "ConsulEE is enabled but not configured properly: " + e.getMessage());
            } finally {
                client.close();
            }

        } else {
            LOGGER.config("ConsulEE is not enabled. Use @EnableConsulEEClient!");
        }
    }

    public void health() {

//        for (;;) {
        LOGGER.config(() -> "health update: " + Calendar.getInstance().getTime());
//        LOGGER.config(() -> "Next: " + timer.getNextTimeout());
        Client client = ClientBuilder.newClient();
        try {
            Response response = client
                    .target(serviceUrl)
                    .path("api")
                    .path("services")
                    .path(applicationConfig.getServiceName())
                    .request()
                    .put(Entity.entity(applicationConfig, APPLICATION_JSON));
        } finally {
            client.close();
        }
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException ex) {
//                LOGGER.config(() -> "Something went wrong: " + ex.getMessage());
//                deregister();
//            }
//        }
    }

    @PreDestroy
    private void deregister() {

        LOGGER.config(() -> "Deregistering " + applicationConfig.getServiceName());

        Client client = ClientBuilder.newClient();
        try {
            Response response = ClientBuilder.newClient()
                    .target(serviceUrl)
                    .path("api")
                    .path("services")
                    .path(applicationConfig.getServiceName())
                    .request()
                    .delete();
        } finally {
            client.close();
        }
    }

    private void readConfiguration() throws ConsulEEConfigurationException {

        Map<String, Object> snoopConfig = Collections.EMPTY_MAP;
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> props = (Map<String, Object>) yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("/snoopee.yml"));

            snoopConfig = (Map<String, Object>) props.get("snoopee");

        } catch (YAMLException e) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }

        applicationConfig.setServiceName(ConsulEEExtensionHelper.getServiceName());
        final String host = readProperty("host", snoopConfig);
        final String port = readProperty("port", snoopConfig);
        applicationConfig.setServiceHome(host + ":" + port + "/");
        applicationConfig.setServiceRoot(readProperty("serviceRoot", snoopConfig));

        LOGGER.config(() -> "application config: " + applicationConfig.toJSON());

        serviceUrl = "http://" + readProperty("snoopeeService", snoopConfig);
    }

    private String readProperty(final String key, Map<String, Object> snoopConfig) {

        String property = Optional.ofNullable(System.getProperty(key))
                .orElseGet(() -> {
                    String envProp = Optional.ofNullable(System.getenv(applicationConfig.getServiceName() + "." + key))
                            .orElseGet(() -> {
                                String confProp = Optional.ofNullable(snoopConfig.get(key))
                                        .orElseThrow(() -> {
                                            return new ConsulEEConfigurationException(key + " must be configured either in snoopee.yml or as env parameter");
                                        })
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });
        return property;
    }

    public void init(@Observes ConsulEEConfig configEvent) {

        LOGGER.config("EVENT");
        TimerTask health = new HealthPing();
        timer = new Timer();
        timer.scheduleAtFixedRate(health, 0, 10000);
    }

    private final class HealthPing extends TimerTask {

        @Override
        public void run() {
            LOGGER.config(() -> "I am healthy!");
            health();
        }
    }

}
