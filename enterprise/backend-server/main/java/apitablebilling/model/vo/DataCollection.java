package com.apitable.enterprise.apitablebilling.model.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * data collection object class.
 *
 * @author Shawn Deng
 */
public abstract class DataCollection<T> implements CollectionInterface<T> {

    protected List<T> data = new ArrayList<>();

    protected Boolean hasMore = false;

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public List<T> getData() {
        return data;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    @Override
    public Boolean getHasMore() {
        return hasMore;
    }
}
