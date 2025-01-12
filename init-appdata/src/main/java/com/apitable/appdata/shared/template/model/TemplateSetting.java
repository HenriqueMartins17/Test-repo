package com.apitable.appdata.shared.template.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TemplateSetting {

    private String name;

    @JsonProperty("tags")
    private List<String> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
