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
package org.efaps.backend.filters;

import java.security.Principal;

import org.keycloak.representations.AccessToken;

import jakarta.ws.rs.core.SecurityContext;

public class KeycloakSecurityContext
    implements SecurityContext
{

    private final AccessToken token;

    protected KeycloakSecurityContext(final AccessToken token)
    {
        this.token = token;
    }

    @Override
    public Principal getUserPrincipal()
    {
        return () -> token.getSubject();
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return false;
    }

    @Override
    public boolean isSecure()
    {
        return true;
    }

    @Override
    public String getAuthenticationScheme()
    {
        return null;
    }
}
