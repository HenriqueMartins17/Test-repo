package com.apitable.enterprise.ai.model;

import static com.apitable.enterprise.ai.model.AiModelEnvironment.ABROAD;
import static com.apitable.enterprise.ai.model.AiModelEnvironment.MAIN_LAND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum AiModel {

    GPT_3_5_TURBO("gpt-3.5-turbo", ABROAD),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", ABROAD),
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613", ABROAD),
    GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613", ABROAD),
    GPT_4("gpt-4", ABROAD),
    GPT_4_0613("gpt-4-0613", ABROAD),
    GPT_4_32K("gpt-4-32k", ABROAD),
    GPT_4_32K_0613("gpt-4-32k-0613", ABROAD),

    CHATGLM2_6B_32K("ChatGLM2-6B-32K", MAIN_LAND),
    ERNIE_BOT("ERNIE-Bot", MAIN_LAND),
    ERNIE_BOT_TURBO("ERNIE-Bot-turbo", MAIN_LAND);


    private final String value;
    private final AiModelEnvironment environment;

    public static AiModel of(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (AiModel model : AiModel.values()) {
            if (model.value.equalsIgnoreCase(value)) {
                return model;
            }
        }
        log.error("Invalid AI model: " + value);
        return null;
    }
}
