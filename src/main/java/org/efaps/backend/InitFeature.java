package org.efaps.backend;

import org.efaps.eql.EQL;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

public class InitFeature implements Feature
{

    @Override
    public boolean configure(FeatureContext context)
    {
        EQL.builder();
        return false;
    }

}
