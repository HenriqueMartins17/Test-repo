package com.apitable.enterprise.apitablebilling.appsumo.handler;

import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;

/**
 * appsumo event handler interface.
 */
public interface IAppsumoEventHandler {
    /**
     * handle event.
     *
     * @param eventLogId event id
     * @return EventVO
     */
    EventVO handle(Long eventLogId);
}
