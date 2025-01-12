package com.apitable.enterprise.infoflow.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Nodes Response.
 */
@Setter
@Getter
public class LinkIdentifyResponse {

    private static final long serialVersionUID = 6941456238190558551L;

    /**
     * Is request successful?.
     */
    private Boolean success;

    /**
     * response status code.
     */
    private Integer code;

    /**
     * response status code's message.
     */
    private String message;

    /**
     * response object.
     */
    private Data[] data;

    /**
     * Data.
     */
    @Setter
    @Getter
    public static class Data {

        private String url;

        private String name;

        private String sourceType;
    }
}
