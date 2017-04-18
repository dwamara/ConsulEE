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

import com.dwitech.eap.consulee.ConsulConfigurationException;
import com.dwitech.eap.consulee.client.ConsulConfig;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.NotRegisteredException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.websocket.ClientEndpoint;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.dwitech.eap.consulee.ConsulExtensionHelper.getServiceName;
import static com.dwitech.eap.consulee.ConsulExtensionHelper.isConsulEnabled;
import static com.orbitz.consul.Consul.builder;
import static java.lang.Integer.valueOf;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.Calendar.getInstance;
import static java.util.Optional.ofNullable;
import static java.util.logging.Logger.getLogger;

/**
 * Registers with Consul and gives heartbeats every 3 second.
 */
@ClientEndpoint @Singleton @Startup
public class ConsulRegistrationClient {
    private static final Logger LOGGER = getLogger("com.dwitech.eap.consulee");
    private static final String REGISTER_ENDPOINT = "consulee";

    private final ConsulConfig applicationConfig = new ConsulConfig();
    private AgentClient agentClient;

    @Resource private TimerService timerService;

    @PostConstruct
    private void init() {
        LOGGER.config("Checking if Consul is enabled");
        if (isConsulEnabled()) {
            try {
                readConfiguration();
                LOGGER.config(() -> "Registering " + applicationConfig.getServiceName());
                register(applicationConfig.getServiceName());
            } catch (ConsulConfigurationException e) {
                LOGGER.severe(() -> "Consul is enabled but not configured properly: " + e.getMessage());
            }
        } else {
            LOGGER.config("Consul is not enabled. Use @EnableConsulClient!");
        }
    }

    public void register(final String clientId) {
        sendMessageToConsul(REGISTER_ENDPOINT, "register");

        ScheduleExpression schedule = new ScheduleExpression();
        schedule.second("*/3").minute("*").hour("*").start(getInstance().getTime());

        TimerConfig config = new TimerConfig();
        config.setPersistent(false);

        Timer timer = timerService.createCalendarTimer(schedule, config);

        LOGGER.config(() -> timer.getSchedule().toString());
    }

    @Timeout
    public void health(Timer timer) {
        LOGGER.config(() -> "health update: " + getInstance().getTime());
        LOGGER.config(() -> "Next: " + timer.getNextTimeout());
        sendMessageToConsul(applicationConfig.getServiceName(), "health");
    }

	@PreDestroy
	private void deregister() {
		LOGGER.config(() -> "Deregistering " + applicationConfig.getServiceId());
		sendMessageToConsul(applicationConfig.getServiceName(), "deregister");
	}

	/**
     * Sends message to the WebSocket server.
     *
     * @param endpoint The server endpoint
     * @param msg The message
     * @return a return message
     */
    private void sendMessageToConsul(String endpoint, String msg) {
        LOGGER.config(() -> "Sending message: " + msg);

        try {
            if (msg.equalsIgnoreCase("register")) {
                agentClient = builder().build().agentClient();
                agentClient.register(valueOf(applicationConfig.getServicePort()), Long.valueOf(applicationConfig.getServiceTTL()), applicationConfig.getServiceName(), applicationConfig.getServiceId());
            }
	        agentClient.pass(applicationConfig.getServiceId());
        } catch (NotRegisteredException ex) {
            LOGGER.warning(ex.getMessage());
        }
    }

    private void readConfiguration() throws ConsulConfigurationException {
        Map<String, Object> consulConfig = Collections.EMPTY_MAP;
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> props = (Map<String, Object>) yaml.load(currentThread().getContextClassLoader().getResourceAsStream("/consul.yml"));
            consulConfig = (Map<String, Object>) props.get("consul");
        } catch (YAMLException yExc) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }

        applicationConfig.setServiceName(getServiceName());
        final String host = readProperty("host", consulConfig);
        final String port = readProperty("port", consulConfig);
        applicationConfig.setServiceHome(host + ":" + port + "/");
        applicationConfig.setServiceRoot(readProperty("serviceRoot", consulConfig));

        LOGGER.config(() -> "application config: " + applicationConfig.toJSON());
    }

    private String readProperty(final String key, Map<String, Object> consulConfig) {
        String property = ofNullable(getProperty(key))
                .orElseGet(() -> {
                    String envProp = ofNullable(System.getenv(applicationConfig.getServiceName() + "." + key))
                            .orElseGet(() -> {
                                String confProp = ofNullable(consulConfig.get(key))
                                        .orElseThrow(() -> new ConsulConfigurationException(key + " must be configured either in snoopee.yml or as env parameter"))
                                        .toString();
                                return confProp;
                            });
                    return envProp;
                });
        return property;
    }
}