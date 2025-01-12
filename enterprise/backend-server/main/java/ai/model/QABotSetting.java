package com.apitable.enterprise.ai.model;

import cn.hutool.json.JSONUtil;
import com.apitable.workspace.dto.DatasheetSnapshot;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Q-A bot setting.
 *
 * @author Shawn Deng
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class QABotSetting implements AiSetting {

    private String viewId;

    private Long revision;

    private Long rows;

    private List<DatasheetSnapshot.Field> fields;

    public static QABotSetting fromJsonString(String jsonString) {
        return JSONUtil.toBean(jsonString, QABotSetting.class);
    }

    @Override
    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
