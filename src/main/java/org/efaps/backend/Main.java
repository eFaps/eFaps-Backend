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
package org.efaps.backend;

import java.io.IOException;
import java.net.URI;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.backend.cluster.GroupCommunication;
import org.efaps.db.Context;
import org.efaps.db.Context.Inheritance;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main
{

    protected static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
        throws InterruptedException, IOException
    {
        SLF4JBridgeHandler.install();

        final var config = ConfigProvider.getConfig();
        final ResourceConfig restConfig = new RestConfig(config);
        new GroupCommunication().init(config);

        final URI baseUri = config.getValue("server.url", URI.class);
        LOG.info("Starting server at: {}", baseUri);
        try {
            Context.begin(null, Inheritance.Local);
            final var server = GrizzlyHttpServerFactory.createHttpServer(baseUri, restConfig, false);
            server.getServerConfiguration().setName("eFaps-Backend");
            Context.rollback();
           server.start();
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }
}
