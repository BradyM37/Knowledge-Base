package com.knowledgebase.model;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;

    public QueryRequest() {
    }

    public QueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}