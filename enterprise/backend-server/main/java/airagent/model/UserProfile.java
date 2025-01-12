package com.apitable.enterprise.airagent.model;

import com.apitable.shared.support.serializer.ImageSerializer;
import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * User profile.
 */
@Data
public class UserProfile {

    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String email;

    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String nickName;

    @JsonSerialize(nullsUsing = NullStringSerializer.class,
        using = ImageSerializer.class)
    private String avatar;

}
