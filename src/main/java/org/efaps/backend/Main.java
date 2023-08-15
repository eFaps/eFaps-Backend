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

package org.efaps.backend;

import java.io.IOException;
import java.net.URI;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.db.Context;
import org.efaps.db.Context.Inheritance;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{

    protected static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
        throws InterruptedException, IOException
    {
        final ResourceConfig restConfig = new RestConfig();

        final var config = ConfigProvider.getConfig();
        final URI baseUri = config.getValue("sever.url", URI.class);
        LOG.info("Starting server at: {}", baseUri);
        try {
            Context.begin(null, Inheritance.Local);
            final var server = GrizzlyHttpServerFactory.createHttpServer(baseUri, restConfig);
            Context.rollback();
            server.start();
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }
}
