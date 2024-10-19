package com.example.accessingdatajpa;

import lombok.Data;

import java.util.Objects;

@Data
public class SearchCriteria implements Comparable<SearchCriteria> {
    public static final String EQUALS="equals";
    private String key;
    private String operation;
    private Object value;
    
    public SearchCriteria(){}
    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    @Override
    public int compareTo(SearchCriteria o) {
        return Objects.equals(key, o.key) && Objects.equals(operation, o.operation) && Objects.equals(value, o.value) ? 0 : key.compareTo(o.key);
    }
}
