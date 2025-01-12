package com.apitable.enterprise.social.notification;

import com.apitable.shared.component.notification.BaseTemplateId;

public enum SocialTemplateId implements BaseTemplateId {

    SPACE_PAID_NOTIFY("space_paid_notify"),

    SPACE_VIKA_PAID_NOTIFY("space_vika_paid_notify");

    private final String value;

    SocialTemplateId(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return null;
    }
}
