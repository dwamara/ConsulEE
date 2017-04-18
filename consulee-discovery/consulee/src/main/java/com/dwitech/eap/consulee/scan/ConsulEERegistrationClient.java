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
import com.dwitech.eap.consulee.ConsulEEExtensionHelper;
import com.dwitech.eap.consulee.client.ConsulEEConfig;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Registers with Consul and gives heartbeats every 3 second.
 */
@ClientEndpoint @Singleton @Startup
public class ConsulEERegistrationClient {
    private static final Logger LOGGER = Logger.getLogger("com.dwitech.eap.consulee");
    private static final String REGISTER_ENDPOINT = "consulee";
    private static final String STATUS_ENDPOINT = "snoopeestatus/";

    private String serviceUrl;
    private final ConsulEEConfig applicationConfig = new ConsulEEConfig();

    @Resource private TimerService timerService;

    @PostConstruct
    private void init() {
        LOGGER.config("Checking if ConsulEE is enabled");
        if (ConsulEEExtensionHelper.isConsulEnabled()) {
            try {
                readConfiguration();
                LOGGER.config(() -> "Registering " + applicationConfig.getServiceName());
                register(applicationConfig.getServiceName());
            } catch (ConsulEEConfigurationException e) {
                LOGGER.severe(() -> "ConsulEE is enabled but not configured properly: " + e.getMessage());
            }
        } else {
            LOGGER.config("ConsulEE is not enabled. Use @EnableConsulEEClient!");
        }
    }

    public void register(final String clientId) {
        sendMessage(REGISTER_ENDPOINT, applicationConfig.toJSON());

        ScheduleExpression schedule = new ScheduleExpression();
        schedule.second("*/3").minute("*").hour("*").start(Calendar.getInstance().getTime());

        TimerConfig config = new TimerConfig();
        config.setPersistent(false);

        Timer timer = timerService.createCalendarTimer(schedule, config);

        LOGGER.config(() -> timer.getSchedule().toString());
    }

    /**
     * Handles incoming message.
     *
     * @param session The WebSocket session
     * @param message The message
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        LOGGER.config(() -> "Message: " + message);
        sendMessage(STATUS_ENDPOINT + applicationConfig.getServiceName(), applicationConfig.toJSON());
    }

    @Timeout
    public void health(Timer timer) {
        LOGGER.config(() -> "health update: " + Calendar.getInstance().getTime());
        LOGGER.config(() -> "Next: " + timer.getNextTimeout());
        sendMessage(STATUS_ENDPOINT + applicationConfig.getServiceName(), applicationConfig.toJSON());
    }

    /**
     * Sends message to the WebSocket server.
     *
     * @param endpoint The server endpoint
     * @param msg The message
     * @return a return message
     */
    private String sendMessage(String endpoint, String msg) {

        LOGGER.config(() -> "Sending message: " + msg);

        String returnValue = "-1";
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = serviceUrl + endpoint;
            Session session = container.connectToServer(this, URI.create(uri));
            session.getBasicRemote().sendText(msg != null ? msg : "");
            returnValue = session.getId();
            
            session.close();

        } catch (DeploymentException | IOException ex) {
            LOGGER.warning(ex.getMessage());
        }

        return returnValue;
    }

    @PreDestroy
    private void deregister() {
        LOGGER.config(() -> "Deregistering " + applicationConfig.getServiceName());
        sendMessage(STATUS_ENDPOINT + applicationConfig.getServiceName(), null);
    }

    private void readConfiguration() throws ConsulEEConfigurationException {
        Map<String, Object> consulConfig = Collections.EMPTY_MAP;
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> props = (Map<String, Object>) yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("/consulee.yml"));
            consulConfig = (Map<String, Object>) props.get("consulee");
        } catch (YAMLException yExc) {
            LOGGER.config(() -> "No configuration file. Using env properties.");
        }

        applicationConfig.setServiceName(ConsulEEExtensionHelper.getServiceName());
        final String host = readProperty("host", consulConfig);
        final String port = readProperty("port", consulConfig);
        applicationConfig.setServiceHome(host + ":" + port + "/");
        applicationConfig.setServiceRoot(readProperty("serviceRoot", consulConfig));

        LOGGER.config(() -> "application config: " + applicationConfig.toJSON());

        serviceUrl = "ws://" + readProperty("snoopeeService", consulConfig);
    }

    private String readProperty(final String key, Map<String, Object> consulConfig) {
        String property = Optional.ofNullable(System.getProperty(key))
                .orElseGet(() -> {
                    String envProp = Optional.ofNullable(System.getenv(applicationConfig.getServiceName() + "." + key))
                            .orElseGet(() -> {
                                String confProp = Optional.ofNullable(consulConfig.get(key))
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
}
