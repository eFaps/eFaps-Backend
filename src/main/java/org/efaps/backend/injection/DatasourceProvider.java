package org.efaps.backend.injection;

import javax.sql.DataSource;

import org.glassfish.hk2.api.Factory;
import org.postgresql.ds.PGSimpleDataSource;

public class DatasourceProvider
    implements Factory<DataSource>
{

    @Override
    public DataSource provide()
    {
        final var ds = new PGSimpleDataSource();
        ds.setURL("jdbc:postgresql://127.0.0.1:5432/lite");
        ds.setUser("efaps");
        ds.setPassword("efaps");
        return ds;
    }

    @Override
    public void dispose(DataSource instance)
    {
        // TODO Auto-generated method stub
    }

    /**
     *
     * username = efaps, password = efaps, jdbcUrl = , maxTotal = -1, maxIdle =
     * 10, removeAbandonedOnBorrow = true, logAbandoned = true, isAutoCommit =
     * false, defaultReadOnly = false, maximumPoolSize = 40
     *
     *
     *
     **/
}
