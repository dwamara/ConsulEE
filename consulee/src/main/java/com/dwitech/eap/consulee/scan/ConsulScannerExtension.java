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

import com.dwitech.eap.consulee.annotation.EnableConsulClient;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.logging.Logger;

import static com.dwitech.eap.consulee.ConsulExtensionHelper.setConsulEnabled;
import static com.dwitech.eap.consulee.ConsulExtensionHelper.setServiceName;

/**
 * CDI Extension that scans for @EnableConsulClient annotations.
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
public class ConsulScannerExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger("com.dwitech.eap.consulee");

    private String serviceName;
    private boolean consulEnabled;

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