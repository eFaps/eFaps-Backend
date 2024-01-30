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
package org.efaps.backend.injection;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.backend.listeners.ContextListener;
import org.glassfish.hk2.api.Factory;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceProvider
    implements Factory<DataSource>
{
    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

    private static DataSource DATASOURCE;

    private void init()
    {
        if (DATASOURCE == null) {
            LOG.debug("Inititalizing datasource:");
            final var config = ConfigProvider.getConfig();
            final var ds = new PGSimpleDataSource();
            ds.setURL(config.getValue("backend.datasource.url", String.class));
            ds.setUser(config.getValue("backend.datasource.user", String.class));
            ds.setPassword(config.getValue("backend.datasource.password", String.class));
            LOG.debug("- {}", ds);
            DATASOURCE = ds;
        }
    }

    @Override
    public DataSource provide()
    {
        init();
        return DATASOURCE;
    }

    @Override
    public void dispose(DataSource instance)
    {
        // nothing to do here?
    }
}
