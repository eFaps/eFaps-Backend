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

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.user.Company;
import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.backend.injection.NoContext;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@Path("first-time-user")
@NoContext
public class FirstTimeUser
{

    private static final Logger LOG = LoggerFactory.getLogger(FirstTimeUser.class);
    private static final String PREFERRED = "preferred_username";
    private static final String COMPANIES = "eFapsCompanies";
    private static final String PERMITCREATEPERSON = "org.efaps.kernel.sso.PermitCreatePerson";
    private static final String PERMITCOMPANYUPDATE = "org.efaps.kernel.sso.PermitCompanyUpdate";

    @GET
    @NoContext
    public Response registUser(@Context final Application app,
                               @Context final HttpHeaders headers)
        throws EFapsException
    {
        LOG.warn("Recieved 'first-time-user' request");
        syncUser(app, headers);
        return Response.ok().build();
    }

    protected void syncUser(final Application app,
                            final HttpHeaders headers)
        throws EFapsException
    {
        final String audience = (String) app.getProperties().get("oidc.audience");
        if (audience == null) {
            LOG.warn("Cannot sync users due to missing 'oidc.audience'");
        } else {
            final var authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            final var token = authHeader.replaceFirst("Bearer ", "");
            try {
                final var jwt = SignedJWT.parse(token);
                final var jwtClaimsSet = jwt.getJWTClaimsSet();
                org.efaps.db.Context.begin();
                if (validatePerson(jwtClaimsSet)) {
                    syncCompanies(jwtClaimsSet);
                    org.efaps.db.Context.commit();
                } else {
                    org.efaps.db.Context.rollback();
                }
            } catch (final ParseException e) {
                LOG.error("Could not parse token or claim", e);
            }
        }
    }

    private boolean validatePerson(final JWTClaimsSet jwtClaimsSet)
        throws EFapsException, ParseException
    {
        LOG.trace("Steping into validatePerson");
        final var subject = jwtClaimsSet.getSubject();
        final Person person = getPerson(subject);
        boolean ret = false;
        if (person != null) {
            ret = true;
        } else if (EFapsSystemConfiguration.get().getAttributeValueAsBoolean(PERMITCREATEPERSON)) {
            LOG.info("{} is activated", PERMITCREATEPERSON);
            final String userName = UUIDUtil.isUUID(jwtClaimsSet.getSubject())
                            ? jwtClaimsSet.getStringClaim(PREFERRED)
                            : jwtClaimsSet.getSubject();
            Person.createPerson(JAASSystem.getJAASSystem("eFaps"), userName, userName,
                            UUIDUtil.isUUID(jwtClaimsSet.getSubject()) ? jwtClaimsSet.getSubject() : null, true);
            ret = true;
            LOG.info("created Person: {}", person);
        } else {
            LOG.warn("Creation of Person is not Permitted for: {}", subject);
        }
        return ret;
    }

    private void syncCompanies(final JWTClaimsSet jwtClaimsSet)
        throws EFapsException, ParseException
    {
        LOG.trace("Steping into syncCompanies");
        if (EFapsSystemConfiguration.get().getAttributeValueAsBoolean(PERMITCOMPANYUPDATE)) {
            LOG.info("{} is activated", PERMITCOMPANYUPDATE);

            final var companyList = jwtClaimsSet.getStringListClaim(COMPANIES);

            if (companyList != null) {

                final Person person = getPerson(jwtClaimsSet.getSubject());
                if (person != null) {
                    final Set<Company> companies = new HashSet<>();
                    for (final String companyStr : companyList) {
                        final Company company;
                        if (UUIDUtil.isUUID(companyStr)) {
                            company = Company.get(UUID.fromString(companyStr));
                        } else {
                            company = Company.get(companyStr);
                        }
                        if (company != null) {
                            companies.add(company);
                        }
                    }
                    LOG.info("Assigning companies {} to user {}", companies, person);
                    final JAASSystem jaasSystem = JAASSystem.getJAASSystem("eFaps");
                    person.setCompanies(jaasSystem, companies);
                }
            }
        }
    }

    private Person getPerson(final String userName)
        throws EFapsException
    {
        final Person person;
        if (UUIDUtil.isUUID(userName)) {
            person = Person.get(UUID.fromString(userName));
        } else {
            person = Person.get(userName);
        }
        return person;
    }
}
