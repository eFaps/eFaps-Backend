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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSecure()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getAuthenticationScheme()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
