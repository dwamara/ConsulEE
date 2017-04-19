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
package com.dwitech.eap.consulee.scan;

import com.dwitech.eap.consulee.annotation.EnableConsulClient;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.logging.Logger;

import static com.dwitech.eap.consulee.ConsulExtensionHelper.setConsulEnabled;
import static com.dwitech.eap.consulee.ConsulExtensionHelper.setServiceName;

/**
 * CDI Extension that scans for @EnableConsulClient annotations.
 */
public class ConsulScannerExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger("com.dwitech.eap.consulee");

    private String serviceName;
    private boolean consulEnabled = false;

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        LOGGER.config("Scanning for Consul clients");
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        LOGGER.config("Discovering Consul clients");
        setServiceName(serviceName);
        setConsulEnabled(consulEnabled);
        LOGGER.config("Finished scanning for Consul clients");
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations(EnableConsulClient.class) ProcessAnnotatedType<T> pat) {
        // workaround for WELD bug revealed by JDK8u60
        final ProcessAnnotatedType<T> consulAnnotated = pat;

        LOGGER.config(() -> "Found @EnableConsulClient annotated class: " + consulAnnotated.getAnnotatedType().getJavaClass().getName());
        consulEnabled = true;
        serviceName = consulAnnotated.getAnnotatedType().getAnnotation(EnableConsulClient.class).serviceName();
        LOGGER.config(() -> "Consul Service name is: " + serviceName);
    }
}