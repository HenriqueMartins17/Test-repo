package com.apitable.enterprise.apitablebilling.model.vo;

import java.util.List;

/**
 * collection interface.
 *
 * @author Shawn Deng
 */
public interface CollectionInterface<T> {

    List<T> getData();

    Boolean getHasMore();
}
