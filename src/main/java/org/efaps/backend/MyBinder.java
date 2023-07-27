package org.efaps.backend;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;

import jakarta.inject.Singleton;
import jakarta.transaction.TransactionManager;

public class MyBinder extends AbstractBinder
{
    @Override
    protected void configure()
    {
        //bindFactory(DatasourceProvider.class).to(DataSource.class).in(Singleton.class);
        bind(TransactionManagerImple.class).to(TransactionManager.class).in(Singleton.class);
    }
}
