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
public class AnonymousFilter implements ContainerRequestFilter
{
    private static final Logger LOG = LoggerFactory.getLogger(AnonymousFilter.class);
    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        LOG.info("Anonymous request: {}", requestContext.getUriInfo().getRequestUri() );
        requestContext.setSecurityContext(new AnonymousSecuritContext());
    }
}
