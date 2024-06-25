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
package org.efaps.backend.filters;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = OpenidConfigurationDto.Builder.class)
public class OpenidConfigurationDto
{

    private final String issuer;

    private final String jwksUri;

    private OpenidConfigurationDto(Builder builder)
    {
        this.issuer = builder.issuer;
        this.jwksUri = builder.jwksUri;
    }

    public String getIssuer()
    {
        return issuer;
    }

    public String getJwksUri()
    {
        return jwksUri;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder
    {

        private String issuer;
        private String jwksUri;

        private Builder()
        {
        }

        public Builder withIssuer(String issuer)
        {
            this.issuer = issuer;
            return this;
        }

        @JsonProperty("jwks_uri")
        public Builder withJwksUri(String jwksUri)
        {
            this.jwksUri = jwksUri;
            return this;
        }

        public OpenidConfigurationDto build()
        {
            return new OpenidConfigurationDto(this);
        }
    }
}
