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
package org.efaps.backend.dto;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = GraphQLPayloadDto.Builder.class)
public class GraphQLPayloadDto
{

    private final String query;
    private final String operationName;
    private final Map<String, Object> variables;

    private GraphQLPayloadDto(Builder builder)
    {
        this.query = builder.query;
        this.operationName = builder.operationName;
        this.variables = builder.variables;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public String getQuery()
    {
        return query;
    }

    public Map<String, Object> getVariables()
    {
        return variables;
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

        private String query;
        private String operationName;
        private Map<String, Object> variables = Collections.emptyMap();

        private Builder()
        {
        }

        public Builder withQuery(String query)
        {
            this.query = query;
            return this;
        }

        public Builder withOperationName(String operationName)
        {
            this.operationName = operationName;
            return this;
        }

        public Builder withVariables(Map<String, Object> variables)
        {
            this.variables = variables;
            return this;
        }

        public GraphQLPayloadDto build()
        {
            return new GraphQLPayloadDto(this);
        }
    }
}
