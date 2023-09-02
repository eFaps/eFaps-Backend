/*
 * Copyright 2003 - 2023 The eFaps Team
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
 *
 */
package org.efaps.backend.injection;

import java.net.MalformedURLException;

import javax.jcr.Repository;

import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.backend.listeners.ContextListener;
import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRepositoryProvider
    implements Factory<Repository>
{

    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

    private static Repository REPOSITORY;

    private void init()
    {
        if (REPOSITORY == null) {
            LOG.debug("Inititalizing JCR Repository:");
            final var config = ConfigProvider.getConfig();
            final var url = config.getValue("backend.jcr.url", String.class);
            try {
                final var repo = new URLRemoteRepository(url);
                LOG.debug("- {}", repo);
                REPOSITORY = repo;
            } catch (final MalformedURLException e) {
                LOG.error("Catched", e);
            }
        }
    }

    @Override
    public Repository provide()
    {
        init();
        return REPOSITORY;
    }

    @Override
    public void dispose(Repository instance)
    {
        // nothing to do here?
    }
}
