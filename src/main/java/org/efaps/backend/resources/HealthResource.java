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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.efaps.backend.injection.Anonymous;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("health")
@Anonymous
public class HealthResource
{

    private static final Logger LOG = LoggerFactory.getLogger(HealthResource.class);
    @Inject
    private DataSource dataSource;

    @GET
    public Response health(@Context ContainerRequestContext context)
        throws EFapsException
    {
        LOG.info("Healthcheck from: {}", context.getUriInfo().getRequestUri());
        boolean dataSourceOK;
        if (dataSource == null) {
            LOG.info("?");
            dataSourceOK = false;
        } else {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                connection = dataSource.getConnection();
                stmt = connection.prepareStatement("Select 1");
                rs = stmt.executeQuery();
                rs.next();
                dataSourceOK = true;
            } catch (final SQLException e) {
                LOG.error("[HealthCheck] failed", e);
                dataSourceOK = false;
            }  finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (final Exception e) {
                        LOG.error("[HealthCheck] Cannot close database connection",
                                e);
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (final Exception e) {
                        LOG.error("[HealthCheck] Cannot close statement", e);
                    }
                }
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (final Exception e) {
                        LOG.error("[HealthCheck] Cannot close result set", e);
                    }
                }
            }
        }
        return dataSourceOK ? Response.ok("More or less " + LocalDateTime.now(), MediaType.TEXT_PLAIN).build()
                        : Response.status(Status.SERVICE_UNAVAILABLE).build();
    }
}
