package com.apitable.enterprise.ai.model;

import java.util.List;
import lombok.Data;

/**
 * Feedback VO.
 */
@Data
public class FeedbackVO {

    private List<Feedback> fb;

    public FeedbackVO(List<Feedback> fb) {
        this.fb = fb;
    }
}
