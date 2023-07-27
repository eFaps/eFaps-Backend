package org.efaps.backend;

import java.time.LocalDateTime;

import org.efaps.util.EFapsException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("health")
public class HealthResource
{

    @GET
    public String health() throws EFapsException {
        return "More or less " + LocalDateTime.now();
    }
}
