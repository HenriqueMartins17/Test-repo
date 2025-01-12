package com.apitable;

import com.apitable.utils.JsonNodeEquals;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GetRecordsTest extends BaseTestSuit{

    @Test
    public void testGetRecordsApi(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBus(dstId);
        JsonNode recordsFromRoomServer = getRecordsFromRoomServer(dstId);
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get all records", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_数字(){
        String dstId = "dstM3ZsUuXHih1AMbj";


        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "数字").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "数字").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records number field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }


    @Test
    public void test_formula_filter_number_field(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus =
                getRecordsFromDataBusWithFieldAndFormula(dstId, "数字", "{数字} >  20")
                .path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithFieldAndFormula(dstId, "数字", "{数字} >  20")
                .path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records formula filter gt", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }


    @Test
    public void test_formula_find_function(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus =
                getRecordsFromDataBusWithFieldAndFormula(dstId, "标题", "find(\"test\", {标题}) > 0")
                .path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithFieldAndFormula(dstId, "标题", "find(\"test\", {标题}) > 0")
                .path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records formula find function", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }


    @Test
    public void test_formula_trim_function(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus =
                getRecordsFromDataBusWithFieldAndFormula(dstId, "标题", "TRIM({标题})=\"test\"")
                .path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithFieldAndFormula(dstId, "标题", "TRIM({标题})=\"test\"")
                .path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records number field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_评分(){
        String dstId = "dstM3ZsUuXHih1AMbj";

        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "评分").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "评分").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records rating field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_公式(){
        String dstId = "dstM3ZsUuXHih1AMbj";

        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "公式").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "公式").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records formula field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_双向关联(){
        String dstId = "dstM3ZsUuXHih1AMbj";

        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "双向关联").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "双向关联").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records two-way-link field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_钱数(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "钱数").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "钱数").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records currency field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_神奇引用(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "神奇引用").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "神奇引用").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records look-up field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_日期(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "日期").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "日期").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records date field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_url(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "url").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "url").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records url field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_多行文本(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "多行文本").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "多行文本").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records multi text field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_邮箱(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "邮箱").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "邮箱").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records email field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }


    }

    @Test
    public void test_选项(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "选项").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "选项").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records select field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_标题(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "标题").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "标题").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records single text field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_自增数字(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "自增数字").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "自增数字").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records auto number field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_天(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "天").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "天").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records check-box field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_电话(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "电话").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "电话").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records phone field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }

    }

    @Test
    public void test_多选(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "多选").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "多选").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records multi-select field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_最后修改时间(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "最后修改时间").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "最后修改时间").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records last modify time field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_创建时间(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "创建时间").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "创建时间").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records creat time field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_最后编辑者(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "最后编辑者").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "最后编辑者").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records last editor field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_创建者(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "创建者").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "创建者").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records creator field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_work_doc(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "doc").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "doc").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records work doc field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_成员(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "成员").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "成员").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records member field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_多级联动(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        JsonNode recordsFromDataBus = getRecordsFromDataBusWithField(dstId, "多级联动").path("data").path("records");
        JsonNode recordsFromRoomServer = getRecordsFromRoomServerWithField(dstId, "多级联动").path("data").path("records");
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records cascader field", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

    @Test
    public void test_视图过滤_包含条件(){
        String dstId = "dstM3ZsUuXHih1AMbj";
        String viewId = "viw5NxBvO37P6";
        JsonNode recordsFromDataBus = getRecordsFromDataBus(dstId, viewId);
        JsonNode recordsFromRoomServer = getRecordsFromRoomServer(dstId, viewId);
        boolean isEq = Objects.equals(recordsFromRoomServer, recordsFromDataBus);
        addRecord("get records filter condition contains", isEq);
        if (isDebug && !isEq) {
            assertNotEquals(new JsonNodeEquals(recordsFromRoomServer), new JsonNodeEquals(recordsFromDataBus));
        }
    }

}
