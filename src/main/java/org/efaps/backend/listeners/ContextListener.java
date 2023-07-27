package org.efaps.backend.listeners;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements RequestEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);


    @Override
    public void onEvent(final RequestEvent event)
    {
        LOG.info("event {}", event.getType());
        switch (event.getType()) {
            case FINISHED:
                LOG.info("Context stop");
                try {
                    if (Context.isThreadActive()) {
                        Context.commit();
                    }
                } catch (final EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case ON_EXCEPTION:
                try {
                    if (Context.isThreadActive()) {
                        Context.rollback();
                    }
                } catch (final EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

}
