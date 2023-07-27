package org.efaps.backend;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

public class InitFeature implements Feature
{

    @Override
    public boolean configure(FeatureContext context)
    {
        return false;
    }

}
