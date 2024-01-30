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
package org.efaps.backend.listeners;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener
    implements RequestEventListener
{

    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

    @Override
    public void onEvent(final RequestEvent event)
    {
        LOG.trace("event {}", event.getType());
        switch (event.getType()) {
            case FINISHED:
                try {
                    if (Context.isThreadActive()) {
                        LOG.debug("Context stop");
                        Context.commit();
                    }
                } catch (final EFapsException e) {
                    LOG.error("FINISHED threw", e);
                }
                break;
            case ON_EXCEPTION:
                try {
                    if (Context.isThreadActive()) {
                        Context.rollback();
                    }
                } catch (final EFapsException e) {
                    LOG.error("ON_EXCEPTION threw", e);
                }
                break;
            default:
                break;
        }
    }
}
