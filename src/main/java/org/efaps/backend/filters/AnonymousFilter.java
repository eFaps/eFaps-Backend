package org.efaps.backend.filters;

import java.io.IOException;

import org.efaps.backend.injection.Anonymous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Anonymous
@Provider
@Priority(Priorities.AUTHENTICATION - 10)
public class AnonymousFilter  implements ContainerRequestFilter
{
    private static final Logger LOG = LoggerFactory.getLogger(AnonymousFilter.class);
    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.info("Anonymous request");
        requestContext.setSecurityContext(new AnonymousSecuritContext());
    }
}
