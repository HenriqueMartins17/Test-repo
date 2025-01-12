package com.apitable.enterprise.ai.model;

import java.util.List;
import lombok.Data;

/**
 * Pagination.
 *
 * @param <T> data type
 */
@Data
public class Pagination<T> {

    private boolean hasMore;

    private List<T> data;

    public Pagination(boolean hasMore, List<T> data) {
        this.hasMore = hasMore;
        this.data = data;
    }

    public static <T> Pagination<T> of(boolean hasMore, List<T> data) {
        return new Pagination<>(hasMore, data);
    }
}
