/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vikabilling.model;

import com.apitable.enterprise.vikabilling.setting.Event;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import lombok.Data;

/**
 * EventVO.
 */
@Data
public class EventVO {

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDate;

    /**
     * .
     */
    public static EventVO of(Event event) {
        EventVO eventVO = new EventVO();
        if (event != null) {
            eventVO.setStartDate(event.getStartDate());
            eventVO.setEndDate(event.getEndDate());
        }
        return eventVO;
    }
}
