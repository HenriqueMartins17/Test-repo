package com.apitable.enterprise.ai.credit;

import static com.apitable.enterprise.ai.model.AiModel.CHATGLM2_6B_32K;
import static com.apitable.enterprise.ai.model.AiModel.ERNIE_BOT;
import static com.apitable.enterprise.ai.model.AiModel.ERNIE_BOT_TURBO;
import static com.apitable.enterprise.ai.model.AiModel.GPT_3_5_TURBO;
import static com.apitable.enterprise.ai.model.AiModel.GPT_3_5_TURBO_0613;
import static com.apitable.enterprise.ai.model.AiModel.GPT_3_5_TURBO_16K;
import static com.apitable.enterprise.ai.model.AiModel.GPT_3_5_TURBO_16K_0613;
import static com.apitable.enterprise.ai.model.AiModel.GPT_4;
import static com.apitable.enterprise.ai.model.AiModel.GPT_4_0613;
import static com.apitable.enterprise.ai.model.AiModel.GPT_4_32K;
import static com.apitable.enterprise.ai.model.AiModel.GPT_4_32K_0613;

import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.AiModelEnvironment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * credit converter.
 */
public class CreditConverter {

    private static final Long charsPerCredit = 300000L;

    private static final Map<AiModel, BigDecimal> queryCreditMappings;
    private static final Map<AiModelEnvironment, Long> embeddingCreditMappings;


    static {
        queryCreditMappings = new HashMap<>();
        queryCreditMappings.put(GPT_3_5_TURBO, new BigDecimal(1));
        queryCreditMappings.put(GPT_3_5_TURBO_0613, new BigDecimal(1));
        queryCreditMappings.put(GPT_3_5_TURBO_16K, new BigDecimal(2));
        queryCreditMappings.put(GPT_3_5_TURBO_16K_0613, new BigDecimal(2));

        queryCreditMappings.put(GPT_4, new BigDecimal(20));
        queryCreditMappings.put(GPT_4_0613, new BigDecimal(20));
        queryCreditMappings.put(GPT_4_32K, new BigDecimal(40));
        queryCreditMappings.put(GPT_4_32K_0613, new BigDecimal(40));

        queryCreditMappings.put(CHATGLM2_6B_32K, BigDecimal.valueOf(0.4));
        queryCreditMappings.put(ERNIE_BOT_TURBO, new BigDecimal(1));
        queryCreditMappings.put(ERNIE_BOT, new BigDecimal(1));

        embeddingCreditMappings = new HashMap<>();
        embeddingCreditMappings.put(AiModelEnvironment.MAIN_LAND, 10000L);
        embeddingCreditMappings.put(AiModelEnvironment.ABROAD, 300000L);
    }

    /**
     * get credit consumed with query.
     *
     * @param model ai model
     * @return credit consumed
     */
    public static BigDecimal creditConsumedWithQuery(AiModel model) {
        if (model == null) {
            throw new RuntimeException("query model is null");
        }
        if (!queryCreditMappings.containsKey(model)) {
            throw new RuntimeException(
                String.format("query model %s is not supported", model.getValue()));
        }
        return queryCreditMappings.get(model);
    }

    /**
     * get credit consumed with training.
     *
     * @param embeddingText embedding text
     * @return credit consumed
     */
    public static BigDecimal creditConsumedWithTraining(EmbeddingText embeddingText) {
        AiModel aiModel = embeddingText.getModel();
        if (aiModel == null) {
            throw new RuntimeException("training model not set");
        }
        AiModelEnvironment environment = aiModel.getEnvironment();
        if (environment == AiModelEnvironment.MAIN_LAND) {
            BigDecimal tokensPerCredit =
                BigDecimal.valueOf(embeddingCreditMappings.get(aiModel.getEnvironment()));
            BigDecimal tokensCount = BigDecimal.valueOf(embeddingText.getTokens());
            return tokensCount.divide(tokensPerCredit, 4, RoundingMode.HALF_UP);
        }
        BigDecimal charsCount = BigDecimal.valueOf(embeddingText.getChars());
        BigDecimal charsPer = BigDecimal.valueOf(charsPerCredit);
        return charsCount.divide(charsPer, 4, RoundingMode.HALF_UP);
    }
}
