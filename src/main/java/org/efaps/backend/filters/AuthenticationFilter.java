package org.efaps.backend.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.info("Starting authentication");
        final var authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null) {
            abortWithUnauthorized(requestContext);
        }
        final var token = authHeader.replaceFirst("Bearer ", "");
        LOG.info("token {}", token);
        final var def = """
            {
              "realm": "demo",
              "auth-server-url": "https://sso.synercom.pe/auth/",
              "ssl-required": "external",
              "resource": "localhost-test",
              "public-client": true,
              "confidential-port": 0
            }""";
        final var targetStream = new ByteArrayInputStream(def.getBytes());
        final var deployment = KeycloakDeploymentBuilder.build(targetStream);

        AccessToken tok;
        try {
            tok = AdapterTokenVerifier.verifyToken(token, deployment);
            LOG.info("tok {}", tok);
        } catch (final VerificationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext)
    {
        requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                        .build());
    }
}
