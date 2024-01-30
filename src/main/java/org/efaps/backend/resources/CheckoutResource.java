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

import org.apache.tika.Tika;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("checkout")
public class CheckoutResource
{
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
    public Response checkout(@QueryParam("oid") final String oid) throws IOException, EFapsException {
        final Instance instance = Instance.get(oid);
        final Checkout checkout = new Checkout(instance);

        final var file = File.createTempFile("Checkout", "");
        final var output = new  FileOutputStream(file);
        checkout.execute(output);
        final ResponseBuilder response = Response.ok(file);
        final Tika tika = new Tika();
        final String mimeType = tika.detect(file);
        response.header("Content-Type", mimeType);
        response.header("Content-Disposition","attachment; filename=\""+ checkout.getFileName() + "\"");
        response.header("Content-Length", checkout.getFileLength());
        return response.build();
    }
}
