package org.efaps.backend.filters;

import java.io.IOException;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION + 10)
public class ContextFilter
    implements ContainerRequestFilter
{

    private static final Logger LOG = LoggerFactory.getLogger(ContextFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        if (requestContext.getSecurityContext() instanceof KeycloakSecurityContext) {
            LOG.info("Context starts here");
            final var sc = (KeycloakSecurityContext) requestContext.getSecurityContext();
            final var userUUID = sc.getUserPrincipal().getName();
            try {
                Context.begin(userUUID);
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
