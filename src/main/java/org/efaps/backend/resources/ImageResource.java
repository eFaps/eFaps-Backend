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
package org.efaps.backend.resources;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;
import org.efaps.backend.injection.Anonymous;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

@Path("/image/{key}")
@Anonymous
public class ImageResource
{

    public static final String CACHENAME = ImageResource.class.getName() + ".Cache";

    private static final Logger LOG = LoggerFactory.getLogger(ImageResource.class);

    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response get(@Context final ContainerRequestContext context,
                        @PathParam("key") final String key)
    {
        LOG.info("Image request from: {}", context.getUriInfo().getRequestUri());
        final var cache = getCache();
        final ResponseBuilder response;
        if (key != null && cache.containsKey(key)) {
            final var file = cache.get(key);
            response = Response.ok(file);
            final Tika tika = new Tika();

            try {
                final var mimeType = tika.detect(file);
                response.header("Access-Control-Expose-Headers", "*");
                response.header("Content-Type", mimeType);
                response.header("Content-Length", file.length());
                response.header("Cache-Control", "max-age=604800");
            } catch (final IOException e) {
                LOG.error("Catched", e);
            }
        } else {
            response = Response.status(Status.NOT_FOUND);
        }
        return response.build();
    }

    private static Cache<String, File> getCache()
    {
        if (!InfinispanCache.get().exists(CACHENAME)) {
            InfinispanCache.get().initCache(CACHENAME);
        }
        return InfinispanCache.get().<String, File>getCache(CACHENAME);
    }
}
