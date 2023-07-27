package org.efaps.backend.listeners;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

public class AppEventListener
    implements ApplicationEventListener
{

    @Override
    public void onEvent(ApplicationEvent event)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent)
    {
        return new ContextListener();
    }
}
