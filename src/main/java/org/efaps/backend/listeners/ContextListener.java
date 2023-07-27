package org.efaps.backend.listeners;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements RequestEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

    public ContextListener() {
        LOG.info("Context start");
    }

    @Override
    public void onEvent(final RequestEvent event)
    {
        LOG.info("event {}", event.getType());
        switch (event.getType()) {
            case START:
                LOG.info("Context start");
                break;
            case FINISHED:
                LOG.info("Context stop");
                break;
            case ON_EXCEPTION:
                LOG.info("Context rollback");
                break;
            default:
                break;
        }

    }

}
