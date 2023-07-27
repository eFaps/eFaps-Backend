package org.efaps.backend.filters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter
    implements ContainerRequestFilter
{

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String CACHENAME = AuthenticationFilter.class.getName() + ".Cache";
    private static KeycloakDeployment KEYCLOAKDEPLOYMENT;

    public AuthenticationFilter()
    {
        init();
    }

    private void init()
    {
        if (!InfinispanCache.get().exists(CACHENAME)) {
            InfinispanCache.get().initCache(CACHENAME);
        }
        if (KEYCLOAKDEPLOYMENT == null) {
            final var config = ConfigProvider.getConfig();
            final var path = config.getValue("keycloak.configFile", java.nio.file.Path.class);
            try {
                KEYCLOAKDEPLOYMENT = KeycloakDeploymentBuilder.build(new FileInputStream(path.toFile()));
            } catch (final FileNotFoundException e) {
                LOG.error("Missing keycloak configuration file", e);
            }
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.info("Starting authentication");
        final var authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null) {
            if (requestContext.getSecurityContext() != null
                            && requestContext.getSecurityContext() instanceof AnonymousSecuritContext) {
                return;
            }
            abortWithUnauthorized(requestContext);
        }
        final var token = authHeader.replaceFirst("Bearer ", "");

        if (getCache().containsKey(token)) {
            LOG.info("found him {}", token);
            final var accessToken = getCache().get(token);
            if (accessToken.isActive()) {
                requestContext.setSecurityContext(new KeycloakSecurityContext(accessToken));
                return;
            }
            getCache().remove(token);
        }
        try {
            final var accessToken = AdapterTokenVerifier.verifyToken(token, KEYCLOAKDEPLOYMENT);
            getCache().put(token, accessToken, 5, TimeUnit.MINUTES);
            LOG.info("tok {}", accessToken);
            requestContext.setSecurityContext(new KeycloakSecurityContext(accessToken));
        } catch (final VerificationException e) {
            LOG.warn("Authentication rejected", e);
            abortWithUnauthorized(requestContext);
        }

    }

    private void abortWithUnauthorized(final ContainerRequestContext requestContext)
    {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private Cache<String, AccessToken> getCache()
    {
        return InfinispanCache.get().getIgnReCache(CACHENAME);
    }
}
