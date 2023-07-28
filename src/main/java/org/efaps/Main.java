package org.efaps;

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

        try {
            Context.begin(null, Inheritance.Local);
            final var server = GrizzlyHttpServerFactory.createHttpServer(baseUri, restConfig);
            Context.rollback();
            server.start();

        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println();
    }
}
