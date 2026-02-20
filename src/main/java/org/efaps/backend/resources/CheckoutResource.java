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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.tika.Tika;
import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.CompletionCallback;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

@Path("checkout")
public class CheckoutResource
{

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutResource.class);
    private static URI TMPURI;
    private static boolean CHECKED;
    private static ExecutorService EXECSERVICE = Executors.newCachedThreadPool(BasicThreadFactory.builder()
                    .namingPattern("CheckoutThread-%d")
                    .build());

    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public void checkout(@Suspended final AsyncResponse asyncResponse,
                         @Context UriInfo uriInfo,
                         @QueryParam("oid") final String oid)
        throws IOException, EFapsException
    {
        if (oid == null) {
            throw new NotAcceptableException();
        }
        final var stopWatch = new StopWatch();
        stopWatch.start();

        final Instance instance = Instance.get(oid);
        final Checkout checkout = new Checkout(instance);
        final File file;
        if (getTmpURI() == null) {
            file = File.createTempFile("Checkout", "");
        } else {
            file = Files.createTempFile(Paths.get(getTmpURI()), "Checkout", "").toFile();
        }
        final var fileOutput = new FileOutputStream(file);
        try {
            checkout.execute(fileOutput);
        } catch (final EFapsException e) {
            asyncResponse.resume(Response.ok(Status.NOT_FOUND).build());
        }

        asyncResponse.register((CompletionCallback) throwable -> {
            stopWatch.stop();
            LOG.info("Checkout with {} finished in {}", uriInfo.getQueryParameters(true),
                            stopWatch.formatTime());
            if (file.exists()) {
                file.delete();
            }
        });

        final String mimeType = new Tika().detect(file);

        EXECSERVICE.execute(() -> {
            final StreamingOutput fileStream = output -> {
                try (var in = new FileInputStream(file)) {
                    final byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
            };
            final ResponseBuilder response = Response.ok(fileStream);
            response.header("Access-Control-Expose-Headers", "*");
            response.header("Content-Type", mimeType);
            response.header("Content-Disposition", "attachment; filename=\"" + checkout.getFileName() + "\"");
            response.header("Content-Length", checkout.getFileLength());
            asyncResponse.resume(Response.ok(fileStream).build());
        });
    }

    private URI getTmpURI()
    {
        if (TMPURI == null && !CHECKED) {
            final var config = ConfigProvider.getConfig();
            final var tempFolder = config.getOptionalValue("core.tmpFolder", java.io.File.class);
            if (tempFolder.isPresent()) {
                LOG.info("found config for tempFolder: {}", tempFolder);
                TMPURI = tempFolder.get().toURI();
            } else {
                LOG.info("no config found");
                TMPURI = null;
            }
            CHECKED = true;
        }
        return TMPURI;
    }
}
