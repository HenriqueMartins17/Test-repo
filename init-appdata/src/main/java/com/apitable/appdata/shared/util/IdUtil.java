package com.apitable.appdata.shared.util;

public class IdUtil {

    private static final Integer ID_FIXED_LENGTH = 10;

    public static final String FOD = "fod";

    private static final String TPL = "tpl";

    private static final String TPC = "tpc";

    private static final String TPT = "tpt";

    private static final String ALB = "alb";

    public static String createFolderId() {
        return FOD + RandomExtendUtil.randomString(ID_FIXED_LENGTH);
    }

    public static String createTemplateId() {
        return TPL + RandomExtendUtil.randomString(ID_FIXED_LENGTH);
    }

    public static String createTempCatCode() {
        return TPC + RandomExtendUtil.randomString(ID_FIXED_LENGTH);
    }

    public static String createTempTagCode() {
        return TPT + RandomExtendUtil.randomString(ID_FIXED_LENGTH);
    }

    public static String createTemplateAlbumId() {
        return ALB + RandomExtendUtil.randomString(ID_FIXED_LENGTH);
    }
}
