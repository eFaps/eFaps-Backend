package org.efaps.backend.resources;

import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Path("version")
public class VersionResource
{
    private static final Logger LOG = LoggerFactory.getLogger(HealthResource.class);

    @GET
    public String health(@Context ContainerRequestContext context) throws EFapsException {
        LOG.info("context: {}", context.getSecurityContext());
        return "1";
    }
}
