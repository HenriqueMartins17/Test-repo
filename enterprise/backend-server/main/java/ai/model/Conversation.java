package com.apitable.enterprise.ai.model;

import com.apitable.shared.support.serializer.CreditUnitSerializer;
import com.apitable.shared.support.serializer.ImageSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * the conversation object.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    /**
     * conversation id.
     */
    private String id;

    /**
     * ai id.
     */
    private String aiId;

    /**
     * training object.
     */
    private String trainingId;

    /**
     * conversation title.
     */
    private String title;

    /**
     * conversation origin.
     */
    private String origin;

    /**
     * conversation created time.
     */
    private Long created;

    /**
     * conversation user.
     */
    private User user;

    /**
     * conversation transaction.
     */
    private Transaction transaction;

    @Getter
    @Setter
    public static class User {

        private String name;

        @JsonSerialize(using = ImageSerializer.class)
        private String avatar;

        public User(String name, String avatar) {
            this.name = name;
            this.avatar = avatar;
        }

        public static User of(String name, String avatar) {
            return new User(name, avatar);
        }
    }

    @Getter
    @Setter
    public static class Transaction {

        @JsonSerialize(using = CreditUnitSerializer.class)
        private BigDecimal totalAmount;

        public Transaction(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
