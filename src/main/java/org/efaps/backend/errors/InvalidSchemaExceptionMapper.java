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
package org.efaps.backend.errors;

import java.time.OffsetDateTime;

import org.efaps.backend.dto.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.schema.validation.InvalidSchemaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidSchemaExceptionMapper
    implements ExceptionMapper<InvalidSchemaException>
{

    private static final Logger LOG = LoggerFactory.getLogger(InvalidSchemaException.class);

    @Override
    public Response toResponse(final InvalidSchemaException exception)
    {
        LOG.error("Error 500 for: {}", exception);
        return Response.serverError().entity(ErrorDto.builder().withDateTime(OffsetDateTime.now())
                        .withMessage(exception.getMessage()).build()).build();
    }

}
