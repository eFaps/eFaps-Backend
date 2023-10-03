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
