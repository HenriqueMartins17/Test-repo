package com.apitable.enterprise.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Transaction Type.
 */
@Getter
@AllArgsConstructor
public enum TransactionType {

    QUERY("query"),
    TRAINING("training");

    private final String value;
}
