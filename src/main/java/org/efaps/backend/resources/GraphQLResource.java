package org.efaps.backend.resources;

import org.apache.commons.lang3.StringUtils;
import org.efaps.backend.dto.GraphQLPayloadDto;
import org.efaps.graphql.EFapsGraphQL;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("graphql")
public class GraphQLResource
{

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLResource.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response get(@QueryParam("query") final String query)
        throws EFapsException
    {
        LOG.info("GraphQL - GET:", query);
        String queryStr;
        if (StringUtils.isEmpty(query)) {
            queryStr = "{ __schema { types { name fields { name } } } }";
        } else {
            queryStr = query;
        }
        LOG.info(queryStr);
        final var executionResult = new EFapsGraphQL().query(queryStr);
        final var object = executionResult.getData() == null ? executionResult.getErrors()
                        : executionResult.getData();
        return Response.ok(object).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public Response post(final GraphQLPayloadDto payload)
        throws EFapsException
    {
        LOG.info("GraphQL - POST:", payload);
        String queryStr;
        if (StringUtils.isEmpty(payload.getQuery())) {
            queryStr = "{ __schema { types { name fields { name } } } }";
        } else {
            queryStr = payload.getQuery();
        }
        LOG.info(queryStr);
        final var executionResult = new EFapsGraphQL().query(queryStr, payload.getOperationName(),
                        payload.getVariables());
        return Response.ok(executionResult.toSpecification()).build();
    }

}
