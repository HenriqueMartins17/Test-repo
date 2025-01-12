package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * feedback query.
 */
@Data
public class FeedbackQuery {

    private Integer pageNum;

    private Integer pageSize;

    private Integer state;

    private String search;

    public FeedbackQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public FeedbackQuery(Integer pageNum, Integer pageSize, Integer state, String search) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.state = state;
        this.search = search;
    }
}
