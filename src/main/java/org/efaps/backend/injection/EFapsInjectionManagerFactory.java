package org.efaps.backend.injection;

import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;

import jakarta.annotation.Priority;

@Priority(100)
public class EFapsInjectionManagerFactory extends Hk2InjectionManagerFactory
{
   @Override
   public InjectionManager create(Object parent)
   {
       final var factory = ServiceLocatorFactory.getInstance();
       final var parentLocator = factory.create("eFaps-Core");
       final var manager = super.create(parentLocator);
       return manager;
   }
}
