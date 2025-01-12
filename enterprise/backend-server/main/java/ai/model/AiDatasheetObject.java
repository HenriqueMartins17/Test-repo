package com.apitable.enterprise.ai.model;

import com.apitable.workspace.dto.DatasheetSnapshot;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI datasheet setting object present AI relation datasheet.
 *
 * @author Shawn Deng
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDatasheetObject extends AbstractAiNode {

    private Long revision;

    private int rows;

    private List<DatasheetSnapshot.Field> fields;
}
