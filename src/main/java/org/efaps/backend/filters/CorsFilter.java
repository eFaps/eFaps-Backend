package org.efaps.backend.filters;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
public class CorsFilter
    implements ContainerRequestFilter, ContainerResponseFilter
{

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        if (isPreflightRequest(requestContext)) {
            requestContext.abortWith(Response.ok().build());
            return;
        }

    }

    private static boolean isPreflightRequest(ContainerRequestContext requestContext)
    {
        return requestContext.getHeaderString("Origin") != null
                        && requestContext.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext)
        throws IOException
    {
        if (requestContext.getHeaderString("Origin") == null) {
            return;
        }

        if (isPreflightRequest(requestContext)) {
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseContext.getHeaders().add("Access-Control-Allow-Headers",
                            "X-Requested-With,x-context-company, Authorization, Accept-Version,Content-Type");
        }
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
    }
}
