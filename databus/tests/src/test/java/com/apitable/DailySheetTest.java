package com.apitable;

import com.apitable.utils.JsonNodeEquals;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DailySheetTest extends BaseTestSuit {


    @Test
    public void getTodayViewDailySheet() {
        String dstId = "dstrmVd9p6ZPMYXbXc";
        String viewId = "viwLDmj5lO1yC";
        JsonNode recordsFromDataBus = getRecordsFromDataBus(dstId, viewId);
        JsonNode recordsFromRoomServer = getRecordsFromRoomServer(dstId, viewId);
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("获取晨会表今日视图", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }


    @Test
    public void getMemberManifestDailySheet() {
        String dstId = "dstrmVd9p6ZPMYXbXc";
        String viewId = "viwpZGW5wj8em";
        JsonNode recordsFromDataBus = getRecordsFromDataBus(dstId, viewId);
        JsonNode recordsFromRoomServer = getRecordsFromRoomServer(dstId, viewId);
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("获取晨会表清单视图", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }
}
