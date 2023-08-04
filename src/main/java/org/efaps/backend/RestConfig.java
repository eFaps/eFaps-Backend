/*
 * Copyright 2003 - 2023 The eFaps Team
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
 *
 */

package org.efaps.backend;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.backend.filters.AnonymousFilter;
import org.efaps.backend.filters.AuthenticationFilter;
import org.efaps.backend.filters.ContextFilter;
import org.efaps.backend.filters.CorsFilter;
import org.efaps.backend.filters.KeycloakSecurityContext;
import org.efaps.backend.injection.CoreBinder;
import org.efaps.backend.listeners.AppEventListener;
import org.efaps.backend.resources.HealthResource;
import org.efaps.backend.resources.VersionResource;
import org.efaps.db.Context;
import org.efaps.db.Context.Inheritance;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.rest.Compile;
import org.efaps.rest.ObjectMapperResolver;
import org.efaps.rest.RestContext;
import org.efaps.rest.RestEQLInvoker;
import org.efaps.rest.Search;
import org.efaps.util.EFapsException;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;

@ApplicationPath("/api")
public class RestConfig
    extends ResourceConfig
{

    private static final Logger LOG = LoggerFactory.getLogger(RestConfig.class);

    public RestConfig()
    {
        init();
    }

    public void init()
    {
        AppAccessHandler.init("backend", Collections.emptySet());
        inject();

        try {
            Context.begin(null, Inheritance.Local);
            RunLevel.init("webapp");
            RunLevel.execute();
            Context.commit();
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }

        LOG.info("Scanning esjps for REST implementations");
        try {
            if (!Context.isThreadActive()) {
                // backend Resources
                registerClasses(AnonymousFilter.class, CorsFilter.class, HealthResource.class, VersionResource.class,
                                AppEventListener.class,
                                AuthenticationFilter.class, KeycloakSecurityContext.class, ContextFilter.class);
                // third party resources

                Context.begin();
                registerClasses(new EsjpScanner().scan(Path.class, Provider.class));
                //core Resources
                registerClasses(Compile.class, RestEQLInvoker.class, RestContext.class,Search.class, ObjectMapperResolver.class);
                if (LOG.isInfoEnabled() && !getClasses().isEmpty()) {
                    final Set<Class<?>> rootResourceClasses = get(Path.class);
                    if (rootResourceClasses.isEmpty()) {
                        LOG.info("No root resource classes found.");
                    } else {
                        logClasses("Root resource classes found:", rootResourceClasses);
                    }

                    final Set<Class<?>> providerClasses = get(Provider.class);
                    if (providerClasses.isEmpty()) {
                        LOG.info("No provider classes found.");
                    } else {
                        logClasses("Provider classes found:", providerClasses);
                    }
                }
                Context.commit();
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // new EsjpScanner().scan(Path.class, Provider.class)
    }

    public void inject()
    {
        final var factory = ServiceLocatorFactory.getInstance();
        final var locator = factory.create("eFaps-Core");
        final var dcs = locator.getService(DynamicConfigurationService.class);
        final var dynConfig = dcs.createDynamicConfiguration();

        new CoreBinder().bind(dynConfig);
        dynConfig.commit();
    }

    private Set<Class<?>> get(final Class<? extends Annotation> _annoclass)
    {
        final Set<Class<?>> s = new HashSet<>();
        for (final Class<?> c : getClasses()) {
            if (c.isAnnotationPresent(_annoclass)) {
                s.add(c);
            }
        }
        return s;
    }

    private void logClasses(final String _text,
                            final Set<Class<?>> _classes)
    {
        final StringBuilder b = new StringBuilder();
        b.append(_text);
        _classes.stream()
                        .map(Class::getName)
                        .sorted()
                        .forEach(clazzName -> b.append('\n').append("  ").append(clazzName));
        LOG.info(b.toString());
    }
}
