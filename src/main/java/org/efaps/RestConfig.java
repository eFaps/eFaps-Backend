package org.efaps;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.admin.program.esjp.EsjpScanner;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.backend.MyBinder;
import org.efaps.backend.filters.AnonymousFilter;
import org.efaps.backend.filters.AuthenticationFilter;
import org.efaps.backend.filters.ContextFilter;
import org.efaps.backend.filters.KeycloakSecurityContext;
import org.efaps.backend.injection.DatasourceProvider;
import org.efaps.backend.listeners.AppEventListener;
import org.efaps.backend.resources.HealthResource;
import org.efaps.backend.resources.VersionResource;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.rest.Compile;
import org.efaps.rest.ObjectMapperResolver;
import org.efaps.rest.RestContext;
import org.efaps.rest.RestEQLInvoker;
import org.efaps.rest.Search;
import org.efaps.util.EFapsException;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.FactoryDescriptorsImpl;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.TransactionManager;
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
        register(new MyBinder());
        init();
    }

    public void init()
    {
        AppAccessHandler.init("backend", Collections.emptySet());
        inject();

        try {
            Context.begin();
            RunLevel.init("webapp");
            RunLevel.execute();
            Context.commit();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOG.info("Scanning esjps for REST implementations");
        try {
            if (!Context.isThreadActive()) {
                // backend Resources
                registerClasses(AnonymousFilter.class, HealthResource.class, VersionResource.class,
                                AppEventListener.class,
                                AuthenticationFilter.class, KeycloakSecurityContext.class, ContextFilter.class);

                Context.begin();
                registerClasses(new EsjpScanner().scan(Path.class, Provider.class));
                registerClasses(Compile.class);
                // registerClasses(Update.class);
                registerClasses(RestEQLInvoker.class);
                registerClasses(RestContext.class);
                registerClasses(Search.class);
                registerClasses(ObjectMapperResolver.class);
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
        final var config = ConfigProvider.getConfig();

        final var factory = ServiceLocatorFactory.getInstance();
        final var locator = factory.create("eFaps-Core");
        final var dcs = locator.getService(DynamicConfigurationService.class);
        final var dynConfig = dcs.createDynamicConfiguration();

        final DescriptorImpl descriptor1 = new DescriptorImpl();
        descriptor1.addAdvertisedContract(AbstractDatabase.class.getName());
        descriptor1.setImplementation(config.getValue("backend.database", String.class));
        descriptor1.setScope("jakarta.inject.Singleton");
        dynConfig.bind(descriptor1);

        final DescriptorImpl descriptor2 = new DescriptorImpl();
        descriptor2.addAdvertisedContract(TransactionManager.class.getName());
        descriptor2.setImplementation(config.getValue("backend.transactionManager", String.class));
        dynConfig.bind(descriptor2);

        final DescriptorImpl retVal4 = new DescriptorImpl();
        retVal4.addAdvertisedContract(Factory.class.getName());
        retVal4.setImplementation(DatasourceProvider.class.getName());

        final DescriptorImpl retVal5 = new DescriptorImpl();
        retVal5.addAdvertisedContract(DataSource.class.getName());
        retVal5.setImplementation(DatasourceProvider.class.getName());
        retVal5.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        final var fac = new FactoryDescriptorsImpl(retVal4, retVal5);

        dynConfig.bind(fac);

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
