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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(CorsFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.debug("Checking if Preflight: ");
        if (isPreflightRequest(requestContext)) {
            LOG.debug("- is Preflight");
            requestContext.abortWith(Response.ok().build());
            return;
        }
    }

    private static boolean isPreflightRequest(final ContainerRequestContext requestContext)
    {
        return requestContext.getHeaderString("Origin") != null
                        && requestContext.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext)
        throws IOException
    {
        LOG.debug("Checking if Preflight ");
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
        LOG.debug("Adding headers: {}",  responseContext.getHeaders());
    }
}
