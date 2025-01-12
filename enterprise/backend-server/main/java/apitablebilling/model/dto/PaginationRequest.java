package com.apitable.enterprise.apitablebilling.model.dto;

/**
 * pagination request parameter.
 *
 * @author Shawn Deng
 */
public class PaginationRequest {

    private final String startingAfter;

    private final String endingBefore;

    private final long limit;

    /**
     * construct.
     *
     * @param startingAfter last one on next page of beginning
     * @param endingBefore  last one on previous page of beginning
     * @param limit         number of objects
     */
    public PaginationRequest(String startingAfter, String endingBefore, long limit) {
        this.startingAfter = startingAfter;
        this.endingBefore = endingBefore;
        this.limit = limit;
    }

    public static PaginationRequest of(String startingAfter, String endingBefore, long limit) {
        return new PaginationRequest(startingAfter, endingBefore, limit);
    }

    public String getStartingAfter() {
        return startingAfter;
    }

    public String getEndingBefore() {
        return endingBefore;
    }

    public long getLimit() {
        return limit;
    }
}
