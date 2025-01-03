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
package org.efaps.backend.errors;

import java.time.OffsetDateTime;

import org.efaps.backend.dto.ErrorDto;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionMapper
    implements ExceptionMapper<Throwable>
{

    private static final Logger LOG = LoggerFactory.getLogger(GeneralExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable throwable)
    {
        LOG.error("Error 500", throwable);
        if (Context.isThreadActive()) {
           try {
            Context.rollback();
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
        }
        if (throwable instanceof WebApplicationException) {
            if (((WebApplicationException) throwable).getResponse() != null) {
                return ((WebApplicationException) throwable).getResponse();
            }
        }
        return Response.serverError().entity(ErrorDto.builder().withDateTime(OffsetDateTime.now())
                        .withMessage("Check logs").build()).build();
    }

}
