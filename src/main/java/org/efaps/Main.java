package org.efaps;

import java.io.IOException;
import java.net.URI;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.UriBuilder;


public class Main {
    protected static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        final URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();

        final ResourceConfig config = new RestConfig();
        final var server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        server.start();

        System.out.println();
    }
}
