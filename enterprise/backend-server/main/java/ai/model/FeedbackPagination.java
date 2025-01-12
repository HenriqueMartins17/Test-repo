package com.apitable.enterprise.ai.model;

import java.util.List;
import lombok.Data;

/**
 * feedback pagination.
 */
@Data
@Deprecated(since = "1.8.0", forRemoval = true)
public class FeedbackPagination {

    private long total;
    private long pageSize;
    private long pageNum;
    private List<Feedback> fb;

    public FeedbackPagination(long total, long pageSize, long pageNum, List<Feedback> fb) {
        this.total = total;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.fb = fb;
    }
}
