package com.apitable.enterprise.airagent.model.training;

/**
 * data source.
 */
public interface DataSource extends DataSourceId {

    DataSourceType getType();

    String toSettingRawJson();
}
