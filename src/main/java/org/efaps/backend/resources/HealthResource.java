package org.efaps.backend.resources;

import java.time.LocalDateTime;

import org.efaps.backend.injection.Anonymous;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Path("health")
@Anonymous
public class HealthResource
{
    private static final Logger LOG = LoggerFactory.getLogger(HealthResource.class);

    @GET
    public String health(@Context ContainerRequestContext context) throws EFapsException {
        LOG.info("context: {}", context.getSecurityContext());
        return "More or less " + LocalDateTime.now();
    }
}
