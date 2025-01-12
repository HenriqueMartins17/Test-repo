package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import com.apitable.enterprise.airagent.model.training.DataSources;

/**
 * data source service interface.
 */
public interface IDataSourceService {

    /**
     * whether agent has data source.
     *
     * @param aiId ai id
     * @return true if it has data source
     */
    boolean hasDataSource(String aiId);

    /**
     * get data sources.
     *
     * @param aiId ai id
     * @return data sources
     */
    DataSources getDataSources(String aiId);

    /**
     * add data sources.
     *
     * @param aiId                   ai id
     * @param dataSourceCreateParams data source create params
     */
    void addDataSources(String aiId, DataSourceCreateParams dataSourceCreateParams);

    /**
     * delete data sources.
     *
     * @param dataSourceId data source id
     */
    void deleteDataSources(String dataSourceId);
}
