package org.efaps.backend.injection;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.glassfish.hk2.api.Factory;
import org.postgresql.ds.PGSimpleDataSource;

public class DatasourceProvider
    implements Factory<DataSource>
{

    private static DataSource DATASOURCE;

    private void init()
    {
        if (DATASOURCE == null) {
            final var config = ConfigProvider.getConfig();
            final var ds = new PGSimpleDataSource();
            ds.setURL(config.getValue("backend.datasource.url", String.class));
            ds.setUser(config.getValue("backend.datasource.user", String.class));
            ds.setPassword(config.getValue("backend.datasource.password", String.class));
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
        // TODO Auto-generated method stub
    }
}
