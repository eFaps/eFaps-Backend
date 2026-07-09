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
package org.efaps.backend.log;

import java.util.Collection;
import java.util.List;

import org.efaps.util.IFormatedLog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

public class FormattingWrappingAppender<E>
    extends ContextAwareBase
    implements Appender<E>
{

    private Appender<E> appender;
    private Context context;
    private String name;

    public void setWrappedAppender(Appender<E> appender)
    {
        this.appender = appender;
        if (context != null) {
            this.appender.setContext(context);
            this.context = null;
        }
        if (name != null) {
            this.appender.setName(name);
            this.name = null;
        }
    }

    @Override
    public void start()
    {
        appender.start();
    }

    @Override
    public void stop()
    {
        appender.stop();
    }

    @Override
    public boolean isStarted()
    {
        return appender.isStarted();
    }

    @Override
    public void setContext(Context context)
    {
        if (appender == null) {
            this.context = context;
        } else {
            appender.setContext(context);
        }
    }

    @Override
    public Context getContext()
    {
        return appender.getContext();
    }

    @Override
    public void addStatus(Status status)
    {
        appender.addStatus(status);
    }

    @Override
    public void addInfo(String msg)
    {
        appender.addInfo(msg);
    }

    @Override
    public void addInfo(String msg,
                        Throwable ex)
    {
        appender.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg)
    {
        appender.addWarn(msg);
    }

    @Override
    public void addWarn(String msg,
                        Throwable ex)
    {
        appender.addWarn(msg, ex);

    }

    @Override
    public void addError(String msg)
    {
        appender.addError(msg);
    }

    @Override
    public void addError(String msg,
                         Throwable ex)
    {
        appender.addError(msg, ex);
    }

    @Override
    public void addFilter(Filter<E> newFilter)
    {
        appender.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters()
    {
        appender.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList()
    {
        return appender.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event)
    {
        return appender.getFilterChainDecision(event);
    }

    @Override
    public String getName()
    {
        return appender.getName();
    }

    @Override
    public void doAppend(E event)
        throws LogbackException
    {
        if (event instanceof final LoggingEvent loggingEvent) {
            final var arguments = loggingEvent.getArgumentArray();
            if (arguments != null) {
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = toObj(loggingEvent.getLevel(), arguments[i]);
                }
            }
        }
        appender.doAppend(event);
    }

    private Object toObj(Level level,
                         Object argument)
    {
        if (argument instanceof final IFormatedLog log) {
            return switch (level.levelInt) {
                case Level.INFO_INT:
                    yield log.logInfo();
                case Level.DEBUG_INT:
                    yield log.logDebug();
                case Level.WARN_INT:
                    yield log.logWarn();
                case Level.TRACE_INT:
                    yield log.logTrace();
                case Level.ERROR_INT:
                    yield log.logError();
                default:
                    yield log.toString();
            };
        } else if (argument instanceof final Collection<?> collection) {
            return collection.stream()
                            .map(value -> toObj(level, value))
                            .toList();
        }
        return argument;
    }

    @Override
    public void setName(String name)
    {
        if (appender == null) {
            this.name = name;
        } else {
            appender.setName(name);
        }
    }
}
