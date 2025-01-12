package com.apitable;

import com.apitable.utils.ReportData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.apitable.utils.PrintTable.printTable;

@SpringBootTest
@SpringBootConfiguration
public class BaseTestSuit {
    protected ObjectMapper mapper = new ObjectMapper();

    public static List<ReportData> contents = new LinkedList<>();

    @Value("${sdk.roomServerHost}")
    protected String roomServerHost;
    protected String spaceId = "spcT925FCEw4d";

    @Value("${sdk.databusServerHost}")
    protected String databusServerHost;

    @Value("${sdk.isDebug}")
    protected boolean isDebug;

    @Value("${sdk.cookie}")
    protected String cookie;

    private static String[] reportHeaders = new String[]{"Test Content", "Result       "};


    public static String LINE_SEP = "\r\n";

    static {
        System.setProperty("line.separator", LINE_SEP);
    }

    public BaseTestSuit(){
    }

    protected void addRecord(
            String testContent,
            boolean isSuccessful
    ) {
        contents.add(new ReportData(testContent, isSuccessful));
    }



    @AfterAll
    public static void allFinished() {
        if (contents.isEmpty()) {
            return;
        }
        int total = contents.size();
        int failed = 0;
        int success = 0;
        List<String[]> tables = new ArrayList<>(total);
        for (ReportData content : contents) {
            if (content.isSuccess) {
                tables.add(new String[]{content.content, "✅"});
                success++;
            }else {
                failed++;
                tables.add(new String[]{content.content, "❌"});
            }
        }
        printTable(reportHeaders, tables);

        double percentage = ((double) success / total) * 100;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedPercentage = decimalFormat.format(percentage);
        String summary = String.format("total test: %d\nfailed test count: %d\nsuccess test count: %d\ntest passing percentage: ", total, failed, success);
        if (total == success) {
            System.out.println(summary + "\u001B[32m" + formattedPercentage + " %" + "\u001B[0m");
        } else {
            System.out.println(summary + "\u001B[31m" + formattedPercentage + " %" + "\u001B[0m");
        }
        contents.clear();
    }

    protected JsonNode getHttpResult(Request request){
        JsonNode result = null;
        int i = 0;
        while (i < 3) {
            try {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String string = response.body().string();
                result = mapper.readTree(string);
                return result;
            } catch (Exception e) {
                i++;
                System.out.println(e.getMessage() + " retrying... " + i + " times");
            }
        }
        return result;
    }


    protected JsonNode getRecordsFromDataBus(String dstId, String viewId) {
        if (viewId == null) {
            viewId = "viwKGKEyrMbNH";
        }
        String url = String.format("%s/fusion/v3/datasheets/%s/records", databusServerHost, dstId);

        Request request = new Request.Builder()
                .url(url + "?viewId=" + viewId)
                .method("GET", null)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer uskM9c6MzfkHMeCJVipM1zv")
                .build();
        return getHttpResult(request);
    }

    protected JsonNode getRecordsFromDataBus(String dstId) {
        return getRecordsFromDataBus(dstId, null);
    }

    protected JsonNode getRecordsFromRoomServer(String dstId, String viewId) {
        if (viewId == null) {
            viewId = "viwKGKEyrMbNH";
        }
        String url = String.format("%s/fusion/v1/datasheets/%s/records", roomServerHost, dstId);
        Request request = new Request.Builder()
                .url(url + "?viewId=" + viewId + "&fieldKey=name")
                .get()
                .addHeader("Authorization", "Bearer uskM9c6MzfkHMeCJVipM1zv")
                .build();
        return getHttpResult(request);
    }

    protected JsonNode getRecordsFromRoomServer(String dstId) {
        return getRecordsFromRoomServer(dstId, null);
    }

    protected JsonNode getRecordsFromDataBusWithField(String dstId, String fieldName) {
        return getRecordsFromDataBusWithFieldAndFormula(dstId, fieldName, null);
    }

    protected JsonNode getRecordsFromDataBusWithFieldAndFormula(String dstId, String fieldName, String formula) {
        String form = "";
        if (formula != null && !formula.isEmpty()) {
            form = "&filterByFormula=" + formula;
        }
        String url = String.format("%s/fusion/v3/datasheets/%s/records", databusServerHost, dstId);

        Request request = new Request.Builder()
                .url(url + "?fields=" + fieldName + form)
                .method("GET", null)
                .addHeader("accept", "application/json")
                .build();
        return getHttpResult(request);
    }

    protected JsonNode getRecordsFromRoomServerWithField(String dstId, String fieldName) {
        return getRecordsFromRoomServerWithFieldAndFormula(dstId, fieldName, null);
    }

    protected JsonNode getRecordsFromRoomServerWithFieldAndFormula(String dstId, String fieldName, String formula) {
        String form = "";
        if (formula != null && !formula.isEmpty()) {
            form = "&filterByFormula=" + formula;
        }
        String url = String.format("%s/fusion/v1/datasheets/%s/records", roomServerHost, dstId);
        Request request = new Request.Builder()
                .url(url + "?viewId=viwKGKEyrMbNH&fieldKey=name&fields=" + fieldName + form)
                .get()
                .addHeader("Authorization", "Bearer uskM9c6MzfkHMeCJVipM1zv")
                .build();
        return getHttpResult(request);
    }

}
