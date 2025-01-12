package com.apitable.utils;

import java.text.BreakIterator;
import java.util.List;

public class PrintTable {

    public static void printTable(List<String[]> data, List<Boolean> flags) {
        String[] headers = {"Left", "Right"};

        int[] columnWidths = getColumnWidths(data, headers);

        printHorizontalLine(columnWidths);
        printRow(false, columnWidths, headers);
        printHorizontalLine(columnWidths);

        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            boolean isRed = !(flags.size() > i ? flags.get(i) : false);
            printRow(isRed, columnWidths, row);
        }

        printHorizontalLine(columnWidths);
    }

    public static void printTable(String[] headers, List<String[]> data) {
        int[] columnWidths = getColumnWidths(data, headers);

        printHorizontalLine(columnWidths);
        printRow(false, columnWidths, headers);
        printHorizontalLine(columnWidths);
        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            printRow(false, columnWidths, row);
        }
        printHorizontalLine(columnWidths);
    }

    public static void printRow(boolean isRed, int[] columnWidths, String... values) {
        System.out.print("| ");
        for (int i = 0; i < columnWidths.length; i++) {
            String value = values[i];
//            if (isRed) {
//                value = "\033[31m" + value + "\033[0m";
//            }
//            sb.append(String.format("%-" + columnWidths[i] + "s",
//                    value
//            ));

            if (isRed) {
                System.out.print("\033[31m");
            }
            System.out.printf("%-" + (columnWidths[i] - getUtf8Count(value)) + "s", value);

            if (isRed) {
                System.out.print("\033[0m");
            }
            System.out.print(" | ");
        }
        System.out.println();
//        System.out.println(sb.toString());
    }

    public static void printHorizontalLine(int[] columnWidths) {
        StringBuilder sb = new StringBuilder("+");

        for (int width : columnWidths) {
            for (int i = 0; i < (width + 2); i++) {
                sb.append("-");
            }
            sb.append("+");
        }

        System.out.println(sb.toString());
    }

    public static int[] getColumnWidths(List<String[]> data, String[] headers) {
        int columnCount = headers.length;
        int[] columnWidths = new int[columnCount];

        for (int k = 0; k < data.size(); k++) {
            String[] row = data.get(k);

            for (int i = 0; i < columnCount; i++) {
                String content = row[i];
                int len = getDisplayLength(content);
                columnWidths[i] = Math.max(columnWidths[i], Math.max(headers[i].length(), len));
            }
        }

        return columnWidths;
    }

    public static int getDisplayLength(String text) {
        BreakIterator iterator = BreakIterator.getCharacterInstance();
        iterator.setText(text);

        int length = 0;
        int start = iterator.first();
        int end = iterator.next();
        while (end != BreakIterator.DONE) {
            String character = text.substring(start, end);
            int charLength = character.matches("[\\u4E00-\\u9FA5]") ? 2 : 1;
            length += charLength;

            start = end;
            end = iterator.next();
        }

        return length;
    }

    public static int getUtf8Count(String text) {
        BreakIterator iterator = BreakIterator.getCharacterInstance();
        iterator.setText(text);

        int length = 0;
        int start = iterator.first();
        int end = iterator.next();
        while (end != BreakIterator.DONE) {
            String character = text.substring(start, end);
            int charLength = character.matches("[\\u4E00-\\u9FA5]") ? 1 : 0;
            length += charLength;

            start = end;
            end = iterator.next();
        }

        return length / 3;
    }

}
