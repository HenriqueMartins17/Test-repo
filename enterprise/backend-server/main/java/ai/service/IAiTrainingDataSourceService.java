package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.model.AiTrainingDataSource;

/**
 * ai data source service.
 */
public interface IAiTrainingDataSourceService {

    /**
     * get data source by ai id.
     *
     * @param aiId ai id
     * @return AiDataSource
     */
    AiTrainingDataSource getDataSourceByAiId(String aiId);

    /**
     * get data source by datasheet id.
     *
     * @param datasheetId datasheet id
     * @return AiDataSource
     */
    AiTrainingDataSource getDataSourceByDatasheetId(String datasheetId);

    /**
     * update data source.
     *
     * @param aiId             ai id
     * @param dataSourceNodeId data source node id
     */
    void updateDataSource(String aiId, String dataSourceNodeId);

    /**
     * update data source setting.
     *
     * @param aiId             ai id
     * @param dataSourceNodeId data source node id
     */
    void updateDataSourceSetting(String aiId, String dataSourceNodeId);
}
