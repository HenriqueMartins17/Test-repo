package com.apitable;

import com.apitable.utils.JsonNodeEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GetDataPackTests extends BaseTestSuit{

    @Test
    public void test_get_datasheet_pack() {
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode dataPackFromRoomServer = getDataPackFromRoomServer(dstId, spaceId);
        JsonNode dataPackFromRoomDataBus = getDataPackFromDataBus(dstId);
        boolean isEq = Objects.equals(dataPackFromRoomServer.path("code"), dataPackFromRoomDataBus.path("code"));
        addRecord("get data pack result code", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(dataPackFromRoomServer.path("code")),
                    new JsonNodeEquals(dataPackFromRoomDataBus.path("code")));
        }

        JsonNode roomServerData = dataPackFromRoomServer.path("data");
        JsonNode dataBusServerData = dataPackFromRoomDataBus.path("data");

        isEq = Objects.equals(roomServerData.path("snapshot"), dataBusServerData.path("snapshot"));
        addRecord("get data pack snapshot", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(roomServerData.path("snapshot")),
                    new JsonNodeEquals(dataBusServerData.path("snapshot")));
        }

        isEq = Objects.equals(roomServerData.path("datasheet"), dataBusServerData.path("datasheet"));
        addRecord("get data pack datasheet", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(roomServerData.path("datasheet")),
                    new JsonNodeEquals(dataBusServerData.path("datasheet")));
        }

        isEq = Objects.equals(roomServerData.path("foreignDatasheetMap").size(), dataBusServerData.path("foreignDatasheetMap").size());
        addRecord("get data pack foreignDatasheetMap size", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(IntNode.valueOf(roomServerData.path("foreignDatasheetMap").size())),
                    new JsonNodeEquals(IntNode.valueOf(dataBusServerData.path("foreignDatasheetMap").size())));
        }
    }


    private JsonNode getDataPackFromRoomServer(String dstId, String spaceId){
        String url = String.format("%s/nest/v1/datasheets/%s/dataPack", databusServerHost, dstId);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Connection", "keep-alive")
                .addHeader("cookie", cookie)
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                .addHeader("X-Socket-Id", "0JaSuJ81H8rXJjejAAAB")
                .addHeader("X-Space-Id", spaceId)
                .addHeader("X-XSRF-TOKEN", "15864ff6-8416-46ae-859a-44a768464225")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .build();
        return getHttpResult(request);
    }


    private JsonNode getDataPackFromDataBus(String dstId) {
        String url = String.format("%s/databus/get_datasheet_pack/%s", databusServerHost, dstId);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .build();
        return getHttpResult(request);
    }

}
