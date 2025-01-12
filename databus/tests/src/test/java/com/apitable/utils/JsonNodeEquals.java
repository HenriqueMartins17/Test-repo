package com.apitable.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

import static com.apitable.BaseTestSuit.LINE_SEP;
import static com.apitable.utils.PrintTable.printTable;

public class JsonNodeEquals {

    private LinkedList<String> paths;
    private JsonNode content;

    public JsonNodeEquals(JsonNode content) {
        this.paths = new LinkedList<>();
        this.content = content;
    }

    public JsonNodeEquals(JsonNode content, LinkedList<String> o) {
        this.paths = new LinkedList<>();
        this.paths.addAll(o);
        this.content = content;
    }

    public JsonNode getContent() {
        return this.content;
    }

    @Override
    public String toString() {
        return content == null ? null : content.toString();
    }

    public boolean equals(Object obj) {
        boolean r = false;
        if (obj instanceof JsonNodeEquals) {
            JsonNodeEquals other = (JsonNodeEquals) obj;
            if (content == null || other.content == null) {
                printDiff((JsonNodeEquals) obj);
            }else {
                r = content.equals(other.getContent());
                if (!r) {
                    if (this.content instanceof ObjectNode && other.getContent() instanceof ObjectNode) {
                        ObjectNode o = (ObjectNode) this.content;
                        ObjectNode o2 = (ObjectNode) other.getContent();
                        for (Iterator<String> it = o.fieldNames(); it.hasNext(); ) {
                            String key = it.next();
                            this.paths.add(key);
                            JsonNodeEquals left = new JsonNodeEquals(o.get(key), this.paths);
                            JsonNodeEquals right = new JsonNodeEquals(o2.get(key), this.paths);
                            this.paths.removeLast();
                            left.equals(right);
                        }
                    } else if (this.content  instanceof ArrayNode && other.getContent() instanceof ArrayNode) {
                        ArrayNode o = (ArrayNode) this.content;
                        ArrayNode o2 = (ArrayNode) other.getContent();
                        int totalSize = Math.max(o.size(), o2.size());
                        for (int i = 0; i < totalSize; i++) {
                            this.paths.add("[" + i + "]");
                            JsonNodeEquals left = new JsonNodeEquals(o.get(i), this.paths);
                            JsonNodeEquals right = new JsonNodeEquals(o2.get(i), this.paths);
                            this.paths.removeLast();
                            left.equals(right);
                        }
                    }else {
                        this.printDiff(other);
                    }
                }
            }
        }else {
            this.printDiff((JsonNodeEquals) obj);
        }

        if (!r && this.paths.isEmpty()) {
            JsonNode otherContent = ((JsonNodeEquals) obj).getContent();
            String leftJsonStr = this.content == null ? "null" : prettyPrint(this.content);
            String rightJsonStr = otherContent == null ? "null" : prettyPrint(otherContent);
            String[] leftStrings = leftJsonStr.split(LINE_SEP);
            String[] rightStrings = rightJsonStr.split(LINE_SEP);
            List<String[]> tables = new ArrayList<>();
            int max = Math.max(leftStrings.length, rightStrings.length);
            LinkedList<Character> leftC = new LinkedList<>();
            LinkedList<Character> rightC = new LinkedList<>();
            List<Boolean> flags = new ArrayList<>();
            for (int i = 0; i < max; i++) {
                String[] row = new String[2];
                String tmpLeftStr = "";
                if (i < leftStrings.length) {
                    tmpLeftStr = leftStrings[i];
                }
                row[0] = tmpLeftStr;
                String tmpRightStr = "";
                if (i < rightStrings.length) {
                    tmpRightStr = rightStrings[i];
                }
                row[1] = tmpRightStr;

                String leftStr = tmpLeftStr.replaceAll("\\s", "");
                String rightStr = tmpLeftStr.replaceAll("\\s", "");
                if (leftStr.endsWith("{")){
                    leftC.add('{');
                }
                if (rightStr.endsWith("{")){
                    rightC.add('{');
                }
                if (leftStr.startsWith("}")){
                    leftC.removeLast();
                }
                if (rightStr.startsWith("}")){
                    rightC.removeLast();
                }
                if (leftC.size() != rightC.size()) {
                    flags.add(false);
                }else {
                    flags.add(Objects.equals(tmpLeftStr, tmpRightStr));
                }
                tables.add(row);
            }
            printTable(tables, flags);
        }
        return r;
    }
    public void printDiff(JsonNodeEquals diff) {
        System.out.println("================  find diff  ================ ");
        System.out.println( "path: " + "\033[1;36m $." +String.join(".", this.paths) + "\033[0m");
        System.out.println("left  content: " + this.content);
        System.out.println("right content: " + diff.getContent());
    }

    private static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        MAPPER.setDefaultPrettyPrinter(prettyPrinter);
    }

    public static String prettyPrint(JsonNode node) {
        String json = null;
        try {
            json = MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
