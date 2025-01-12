package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * pagination build.
 */
@Data
public class Pageable {

    private String cursor;

    private int limit;

    public Pageable(String cursor, int limit) {
        this.cursor = cursor;
        this.limit = limit;
    }

    public static Pageable of(String cursor, int limit) {
        return new Pageable(cursor, limit);
    }

    public static Pageable first(int limit) {
        return new Pageable(null, limit);
    }
}
