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

import com.dwitech.eap.consulee.annotation.EnableConsulEEClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * CDI Extension that scans for @EnableConsulEEClient annotations.
 *
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
public class ConsulEEScannerExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoopee");

    private String serviceName;
    private boolean snoopEnabled;
    
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        LOGGER.config("Scanning for ConsulEE clients");
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {

        LOGGER.config("Discovering ConsulEE clients");
        ConsulEEExtensionHelper.setServiceName(serviceName);
        ConsulEEExtensionHelper.setSnoopEnabled(snoopEnabled);

        AnnotatedType<ConsulEERegistrationClient> at = bm.createAnnotatedType(ConsulEERegistrationClient.class);
        final InjectionTarget<ConsulEERegistrationClient> it = bm.createInjectionTarget(at);

        abd.addBean(new Bean<ConsulEERegistrationClient>() {
            @Override
            public Class<?> getBeanClass() {
                return ConsulEERegistrationClient.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            @Override
            public boolean isNullable() {
                return false;
            }

            @Override
            public ConsulEERegistrationClient create(CreationalContext<ConsulEERegistrationClient> creationalContext) {
                ConsulEERegistrationClient instance = it.produce(creationalContext);
                it.inject(instance, creationalContext);
                it.postConstruct(instance);

                return instance;
            }

            @Override
            public void destroy(ConsulEERegistrationClient instance, CreationalContext<ConsulEERegistrationClient> creationalContext) {
                it.preDestroy(instance);
                it.dispose(instance);
                creationalContext.release();
            }

            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(ConsulEERegistrationClient.class);
                types.add(Object.class);
                return types;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add(new AnnotationLiteral<Default>() {
                });
                qualifiers.add(new AnnotationLiteral<Any>() {
                });
                return qualifiers;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return ApplicationScoped.class;
            }

            @Override
            public String getName() {
                return "snoopEERegistrationClient";
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

        });


        LOGGER.config("Finished scanning for ConsulEE clients");
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations(EnableConsulEEClient.class) ProcessAnnotatedType<T> pat) {

        // workaround for WELD bug revealed by JDK8u60
        final ProcessAnnotatedType<T> snoopAnnotated = pat;

        LOGGER.config(() -> "Found @EnableConsulEEClient annotated class: " + snoopAnnotated.getAnnotatedType().getJavaClass().getName());
        snoopEnabled = true;
        serviceName = snoopAnnotated.getAnnotatedType().getAnnotation(EnableConsulEEClient.class).serviceName();
        LOGGER.config(() -> "ConsulEE Service name is: " + serviceName);
    }
}
