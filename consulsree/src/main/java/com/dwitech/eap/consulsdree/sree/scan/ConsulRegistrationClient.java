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
package com.dwitech.eap.consulsdree.sree.scan;

import com.dwitech.eap.consulee.ConsulConfigurationException;
import com.dwitech.eap.consulee.client.ConsulConfig;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.NotRegisteredException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.dwitech.eap.consulee.ConsulExtensionHelper.getServiceName;
import static com.dwitech.eap.consulee.ConsulExtensionHelper.isConsulEnabled;
import static com.orbitz.consul.Consul.builder;
import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.lang.Thread.currentThread;
import static java.util.Calendar.getInstance;
import static java.util.Optional.ofNullable;
import static java.util.logging.Logger.getLogger;

/**
 * Registers with Consul and gives heartbeats every 3 second.
 * @since 1.0.0
 */
@Singleton @Startup
public class ConsulRegistrationClient {
    private static final Logger LOGGER = getLogger(ConsulRegistrationClient.class.getName());

    private final ConsulConfig consulConfig = new ConsulConfig();
    private AgentClient agentClient;

    @Resource private TimerService timerService;

    @PostConstruct
    private void init() {
        LOGGER.config("Checking if Consul is enabled");
        if (isConsulEnabled()) {
            try {
                readConfiguration();
                LOGGER.config(() -> "Registering " + consulConfig.getServiceName());
                agentClient = builder().withHostAndPort(getConsulHostAndPort()).build().agentClient();
                register();
            } catch (ConsulConfigurationException ccExc) {
                LOGGER.severe(() -> "Consul is enabled but not configured properly: " + ccExc.getMessage());
            }
        } else {
            LOGGER.config("Consul is not enabled. Use @EnableConsulClient!");
        }
    }

    public void register() {
        agentClient.register(valueOf(consulConfig.getServicePort()), Long.valueOf(consulConfig.getServiceTTL()), consulConfig.getServiceName(), consulConfig.getServiceId());

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.second("*/3").minute("*").hour("*").start(getInstance().getTime());

        final TimerConfig config = new TimerConfig();
        config.setPersistent(false);

        final Timer timer = timerService.createCalendarTimer(schedule, config);

        LOGGER.config(() -> timer.getSchedule().toString());
    }

    @Timeout
    public void health(Timer timer) {
        LOGGER.config(() -> "health update: " + getInstance().getTime());
        LOGGER.config(() -> "Next: " + timer.getNextTimeout());
        try {
            agentClient.pass(consulConfig.getServiceId());
        } catch (NotRegisteredException nrExc) {
            nrExc.printStackTrace();
        }
    }

	@PreDestroy
	private void deregister() {
		LOGGER.config(() -> "Deregistering " + consulConfig.getServiceId());
        try {
            agentClient.pass(consulConfig.getServiceId());
        } catch (NotRegisteredException nrExc) {
            nrExc.printStackTrace();
        }
    }

    private HostAndPort getConsulHostAndPort() {
        return HostAndPort.fromParts(consulConfig.getConsulHost(), Integer.parseInt(consulConfig.getConsulPort())).withDefaultPort(8500);
    }

    private void readConfiguration() throws ConsulConfigurationException {
        Map<String, Object> consulConfig = Collections.EMPTY_MAP;
        try {
            final Yaml yaml = new Yaml();
            final Map<String, Object> props = (Map<String, Object>) yaml.load(currentThread().getContextClassLoader().getResourceAsStream("/consul.yml"));
            consulConfig = (Map<String, Object>) props.get("consul");
        } catch (YAMLException yExc) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }

        this.consulConfig.setServiceName(getServiceName());

        this.consulConfig.setServiceId(readProperty("serviceId", consulConfig));
        //this.consulConfig.setServiceName(readProperty("serviceName", consulConfig));
        this.consulConfig.setServiceHost(readProperty("serviceHost", consulConfig));
        this.consulConfig.setServicePort(readProperty("servicePort", consulConfig));
        this.consulConfig.setServiceTTL(readProperty("serviceTTL", consulConfig));

        this.consulConfig.setConsulHost(readProperty("consulHost", consulConfig));
        this.consulConfig.setConsulPort(readProperty("consulPort", consulConfig));

        LOGGER.config(() -> "application config: " + this.consulConfig.toJSON());
    }

    private String readProperty(final String key, Map<String, Object> loadedConfig) {
        String property = ofNullable(getProperty(key))
                .orElseGet(() -> {
                    String envProp = ofNullable(getenv(this.consulConfig.getServiceName() + "." + key))
                            .orElseGet(() -> {
                                String confProp = ofNullable(loadedConfig.get(key))
                                        .orElseThrow(() -> new ConsulConfigurationException(key + " must be configured either in consul.yml or as env parameter"))
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });
        return property;
    }
}