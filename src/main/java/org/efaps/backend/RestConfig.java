/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
package org.efaps.backend;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.backend.converter.ConverterProvider;
import org.efaps.backend.errors.GeneralExceptionMapper;
import org.efaps.backend.errors.InvalidSchemaExceptionMapper;
import org.efaps.backend.features.RequestLogging;
import org.efaps.backend.filters.AnonymousFilter;
import org.efaps.backend.filters.AuthenticationFilter;
import org.efaps.backend.filters.ContextFilter;
import org.efaps.backend.filters.CorsFilter;
import org.efaps.backend.filters.NoContextFilter;
import org.efaps.backend.injection.CoreBinder;
import org.efaps.backend.listeners.AppEventListener;
import org.efaps.backend.resources.CheckoutResource;
import org.efaps.backend.resources.FirstTimeUser;
import org.efaps.backend.resources.GraphQLResource;
import org.efaps.backend.resources.HealthResource;
import org.efaps.backend.resources.ImageResource;
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

    public RestConfig(final Config config)
    {
        init(config);
    }

    public void init(final Config config)
    {
        final var appKey = config.getOptionalValue("backend.appkey", String.class).orElse("backend");
        LOG.info("Initializing AppAccessHandler with: '{}'", appKey);
        AppAccessHandler.init(appKey, Collections.emptySet());
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
                registerClasses(AppEventListener.class, CorsFilter.class, AnonymousFilter.class,
                                AuthenticationFilter.class, NoContextFilter.class,
                                ContextFilter.class, GeneralExceptionMapper.class,
                                InvalidSchemaExceptionMapper.class, ConverterProvider.class,
                                HealthResource.class, VersionResource.class, GraphQLResource.class,
                                CheckoutResource.class, ImageResource.class, FirstTimeUser.class, RequestLogging.class);

                Context.begin();
                registerClasses(new EsjpScanner().scan(Path.class, Provider.class));
                // core Resources
                registerClasses(Compile.class, RestEQLInvoker.class, RestContext.class, Search.class,
                                ObjectMapperResolver.class);
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
            LOG.error("Catched", e);
        }
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
