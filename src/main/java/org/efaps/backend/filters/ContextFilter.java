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
package org.efaps.backend.filters;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.db.Context.Inheritance;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
@Provider
@Priority(Priorities.AUTHENTICATION + 10)
public class ContextFilter
    implements ContainerRequestFilter
{

    public static String HEADER_KEY = "X-CONTEXT-COMPANY";

    private static final Logger LOG = LoggerFactory.getLogger(ContextFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext)
        throws IOException
    {
        if (requestContext.getSecurityContext() instanceof KeycloakSecurityContext) {
            LOG.debug("Context starts here");
            final var sc = (KeycloakSecurityContext) requestContext.getSecurityContext();
            final var userUUID = sc.getUserPrincipal().getName();
            try {
                Context.begin(userUUID, Inheritance.Local);
                if (Context.getThreadContext().getCompany() == null) {
                    final var companyId = Context.getThreadContext().getPerson().getCompanies().stream().sorted()
                                    .findFirst().orElseThrow();
                    Context.getThreadContext().setCompany(Company.get(companyId));
                }
            } catch (final EFapsException e) {
                LOG.error("Something went wrong while setting the context", e);
            }

            try {
                final String companyStr = requestContext.getHeaderString(HEADER_KEY);
                if (StringUtils.isNotEmpty(companyStr)) {
                    LOG.debug("Received Header to set company {}", companyStr);
                    Company company = null;
                    if (UUIDUtil.isUUID(companyStr)) {
                        company = Company.get(UUID.fromString(companyStr));
                    } else if (StringUtils.isNumeric(companyStr)) {
                        company = Company.get(Long.valueOf(companyStr));
                    } else {
                        company = Company.get(companyStr);
                    }
                    if (company == null) {
                        LOG.warn("Received Header to set company {} but could not find the company", companyStr);
                    } else if (Context.getThreadContext().getPerson().getCompanies().contains(company.getId())) {
                        final Company currentCompany = Context.getThreadContext().getCompany();
                        if (currentCompany.getId() == company.getId()) {
                            LOG.debug("Context company unchanged");
                        } else {
                            Context.getThreadContext().setCompany(company);
                            LOG.debug("Set context company to {}", company);
                        }
                    } else {
                        throw new RuntimeException("invalid Company");
                    }
                }
            } catch (final EFapsException e) {
                LOG.error("Something went wrong while setting the company", e);
            }
        }
    }
}
