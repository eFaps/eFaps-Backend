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
package org.efaps.backend.dto;

import java.time.OffsetDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ErrorDto.Builder.class)
public class ErrorDto
{

    private final OffsetDateTime dateTime;

    private final String message;

    private ErrorDto(Builder builder)
    {
        this.dateTime = builder.dateTime;
        this.message = builder.message;
    }

    public OffsetDateTime getDateTime()
    {
        return dateTime;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {

        private OffsetDateTime dateTime;
        private String message;

        private Builder()
        {
        }

        public Builder withDateTime(OffsetDateTime dateTime)
        {
            this.dateTime = dateTime;
            return this;
        }

        public Builder withMessage(String message)
        {
            this.message = message;
            return this;
        }

        public ErrorDto build()
        {
            return new ErrorDto(this);
        }
    }
}
