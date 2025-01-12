package com.apitable.enterprise.ai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Transaction Type.
 *
 * @author Shawn Deng
 */
@AllArgsConstructor
@Getter
public enum CreditTransactionType {

    QUERY("query"), TRAIN("train");

    private final String value;
}
