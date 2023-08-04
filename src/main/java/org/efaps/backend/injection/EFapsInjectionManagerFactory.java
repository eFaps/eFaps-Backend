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

import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;

import jakarta.annotation.Priority;

@Priority(100)
public class EFapsInjectionManagerFactory
    extends Hk2InjectionManagerFactory
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
