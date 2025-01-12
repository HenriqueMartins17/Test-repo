package com.apitable.enterprise.ai.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * get suggestion params.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GetSuggestionParams {

    private String question;
    private int n;
}
