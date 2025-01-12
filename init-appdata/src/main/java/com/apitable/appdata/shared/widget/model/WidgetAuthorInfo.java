package com.apitable.appdata.shared.widget.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WidgetAuthorInfo {

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_email")
    private String authorEmail;

    @JsonProperty("author_icon")
    private String authorIcon;

    @JsonProperty("author_link")
    private String authorLink;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorIcon() {
        return authorIcon;
    }

    public void setAuthorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
    }

    public String getAuthorLink() {
        return authorLink;
    }

    public void setAuthorLink(String authorLink) {
        this.authorLink = authorLink;
    }
}
