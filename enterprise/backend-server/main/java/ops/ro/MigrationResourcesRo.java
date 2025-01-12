package com.apitable.enterprise.ops.ro;

import com.apitable.core.support.deserializer.StringOrArrayToStringArrayDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "migration resources request param")
public class MigrationResourcesRo {

    @Schema(description = "Auth Token", requiredMode = Schema.RequiredMode.REQUIRED, example = "K9vvkTLy2eaViE4BAjpuCHEn")
    private String token;

    @Schema(description = "sourceBucket", requiredMode = Schema.RequiredMode.REQUIRED, example = "vk-assets-ltd")
    private String sourceBucket;

    @Schema(description = "targetBucket", requiredMode = Schema.RequiredMode.REQUIRED, example = "vk-datasheet")
    private String targetBucket;

    @Schema(description = "resourceKeys", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringOrArrayToStringArrayDeserializer.class)
    private List<String> resourceKeys;

}