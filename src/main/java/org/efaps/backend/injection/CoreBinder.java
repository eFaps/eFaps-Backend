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

import javax.jcr.Repository;
import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.db.databases.AbstractDatabase;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.TransactionManager;

public class CoreBinder
    extends AbstractBinder
{

    private static final Logger LOG = LoggerFactory.getLogger(CoreBinder.class);

    @Override
    protected void configure()
    {
        try {
            final var config = ConfigProvider.getConfig();

            final var database = Class.forName(config.getValue("backend.database", String.class));
            bind(database).to(AbstractDatabase.class).in(jakarta.inject.Singleton.class);

            final var transactionManager = Class.forName(config.getValue("backend.transaction.manager", String.class));
            bind(transactionManager).to(TransactionManager.class);

            bind(config.getValue("backend.transaction.timeout", Integer.class)).to(Integer.class)
                            .named("transactionManagerTimeOut");

            bindFactory(DatasourceProvider.class).to(DataSource.class);
            bindFactory(JCRRepositoryProvider.class).to(Repository.class);
        } catch (final ClassNotFoundException e) {
            LOG.error("Catched", e);
        }
    }
}
