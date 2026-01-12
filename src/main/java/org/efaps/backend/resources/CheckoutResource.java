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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.tika.Tika;
import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

@Path("checkout")
public class CheckoutResource
{

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutResource.class);
    private URI tmpURI;

    public CheckoutResource()
    {
        LOG.info("Trying to read tmpFolder from config 'core.tmpFolder'");
        final var config = ConfigProvider.getConfig();
        final var tempFolder = config.getOptionalValue("core.tmpFolder", java.io.File.class);
        if (tempFolder.isPresent()) {
            LOG.info("found config for tempFolder: {}", tempFolder);
            this.tmpURI = tempFolder.get().toURI();
        } else {
            LOG.info("no config found");
            this.tmpURI = null;
        }
    }

    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public Response checkout(@QueryParam("oid") final String oid)
        throws IOException, EFapsException
    {
        final Instance instance = Instance.get(oid);
        final Checkout checkout = new Checkout(instance);
        final File file;
        if (this.tmpURI == null) {
            file = File.createTempFile("Checkout", "");
        } else {
            file = Files.createTempFile(Paths.get(this.tmpURI), "Checkout", "").toFile();
        }

        final var output = new FileOutputStream(file);
        try {
            checkout.execute(output);
        } catch (final EFapsException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        final ResponseBuilder response = Response.ok(file);
        final Tika tika = new Tika();
        final String mimeType = tika.detect(file);
        response.header("Access-Control-Expose-Headers", "*");
        response.header("Content-Type", mimeType);
        response.header("Content-Disposition", "attachment; filename=\"" + checkout.getFileName() + "\"");
        response.header("Content-Length", checkout.getFileLength());
        return response.build();
    }
}
