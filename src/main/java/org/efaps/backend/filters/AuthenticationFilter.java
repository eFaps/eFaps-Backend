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
        LOG.debug("Starting authentication");
        final var authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null) {
            if (requestContext.getSecurityContext() != null
                            && requestContext.getSecurityContext() instanceof AnonymousSecuritContext) {
                return;
            }
            abortWithUnauthorized(requestContext);
            return;
        }
        final var token = authHeader.replaceFirst("Bearer ", "");

        if (getCache().containsKey(token)) {
            LOG.debug("retrieved token from Cache {}", token);
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
            LOG.debug("added token to Cache {}", accessToken);
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
