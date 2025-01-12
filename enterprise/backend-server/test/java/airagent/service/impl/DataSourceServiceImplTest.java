package com.apitable.enterprise.airagent.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.airagent.model.training.DataSource;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParam;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import com.apitable.enterprise.airagent.model.training.DataSourceType;
import com.apitable.enterprise.airagent.model.training.DataSources;
import org.junit.jupiter.api.Test;

public class DataSourceServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testHasDataSource() {
        boolean has = iDataSourceService.hasDataSource("test");
        assertThat(has).isFalse();
    }

    @Test
    void testAddDataSources() {
        MockData mockData = createMockData();
        String aiId = createAiNode(mockData.getUserSpace().getSpaceId());
        DataSourceCreateParams params = new DataSourceCreateParams();
        // add airtable data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.AIRTABLE)
                .airtable(
                    DataSourceCreateParam.Airtable.builder()
                        .apiKey("key")
                        .baseId("baseId")
                        .tableId("tableName")
                        .build()
                )
                .build()
        );
        // add aitable data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.AITABLE)
                .aitable(
                    DataSourceCreateParam.Aitable.builder()
                        .apiKey("key")
                        .datasheetId("datasheetId")
                        .viewId("viewId")
                        .build()
                )
                .build()
        );
        // add file data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.FILE)
                .file(
                    DataSourceCreateParam.File.builder()
                        .name("file1.pdf")
                        .url("2023/01/01/file1.pdf")
                        .build()
                )
                .build()
        );
        // add datasheet data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.DATASHEET)
                .datasheet(
                    DataSourceCreateParam.Datasheet.builder()
                        .datasheetId(mockData.getDatasheetId())
                        .viewId(mockData.getViewId())
                        .build()
                )
                .build()
        );
        iDataSourceService.addDataSources(aiId, params);

        DataSources dataSources = iDataSourceService.getDataSources(aiId);
        assertThat(dataSources).isNotEmpty().hasSize(params.size())
            .extracting(DataSource::getType)
            .containsExactly(
                DataSourceType.AIRTABLE,
                DataSourceType.AITABLE,
                DataSourceType.FILE,
                DataSourceType.DATASHEET
            );
    }
}
