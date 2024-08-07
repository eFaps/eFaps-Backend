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
package org.efaps.backend.filters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter
    implements ContainerRequestFilter
{

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);
    private JWKSource<SecurityContext> jwkSource;
    private String oidcAudience;

    public AuthenticationFilter()
    {
        init();
    }

    private void init()
    {
        final var config = ConfigProvider.getConfig();

        final var endpointURI = config.getValue("oidc.configEndpoint", URI.class);
        this.oidcAudience = config.getValue("oidc.audience", String.class);
        LOG.info("Loading oidc from: {}", endpointURI);

        final Client client = ClientBuilder.newClient()
                        .register(JacksonFeature.class);
        final var openidConfiguration = client.target(endpointURI).request().buildGet()
                        .invoke(OpenidConfigurationDto.class);
        LOG.info("Using: {}", openidConfiguration);

        try {
            final var url = new URL(openidConfiguration.getJwksUri());
            this.jwkSource = JWKSourceBuilder.create(url)
                            .cache(true)
                            .build();
        } catch (final MalformedURLException e) {
            LOG.error("Could not parse jwksUri", e);
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.debug("Starting authentication");
        final var authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            if (requestContext.getSecurityContext() != null
                            && requestContext.getSecurityContext() instanceof AnonymousSecuritContext) {
                return;
            }
            abortWithUnauthorized(requestContext);
            return;
        }
        final var token = authHeader.replaceFirst("Bearer ", "");
        try {
            final var jwt = SignedJWT.parse(token);
            final var selector = JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(jwkSource);
            final var keys = selector.selectJWSKeys(jwt.getHeader(), null);
            if (keys.isEmpty()) {
                LOG.warn("Authentication rejected due to invliad JWSKEy");
                abortWithUnauthorized(requestContext);
            } else {
                final var key = keys.get(0);
                final var verifier = new RSASSAVerifier((RSAPublicKey) key);
                if (jwt.verify(verifier) && verifyClaims(jwt.getJWTClaimsSet())) {
                    requestContext.setSecurityContext(
                                    new OidcSecurityContext(jwt.getJWTClaimsSet().getSubject()));
                } else {
                    LOG.warn("Authentication rejected due to token: {}", token);
                    abortWithUnauthorized(requestContext);
                }
            }
        } catch (final ParseException | JOSEException e) {
            LOG.warn("Authentication rejected", e);
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean verifyClaims(final JWTClaimsSet jwtClaimsSet)
        throws ParseException
    {
        LOG.debug("Verifying Subject claim");
        if (StringUtils.isEmpty(jwtClaimsSet.getSubject())) {
            LOG.warn("Authentication rejected due to missing subject: {}", jwtClaimsSet);
            return false;
        }
        LOG.debug("Verifying azp claim");
        if (StringUtils.isNotEmpty(jwtClaimsSet.getStringClaim("azp"))
                        && jwtClaimsSet.getStringClaim("azp").equals(oidcAudience)) {
            return true;
        }
        LOG.debug("Verifying audience claim");
        if (jwtClaimsSet.getAudience().contains(oidcAudience)) {
            return true;
        }
        LOG.warn("Claims verification failed");
        return false;
    }

    private void abortWithUnauthorized(final ContainerRequestContext requestContext)
    {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
