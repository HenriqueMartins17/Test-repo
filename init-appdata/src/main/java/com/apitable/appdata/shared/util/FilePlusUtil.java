package com.apitable.appdata.shared.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.appdata.shared.starter.oss.OssProperties;
import com.qiniu.cdn.CdnManager;
import com.qiniu.common.QiniuException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePlusUtil {

    private static final Logger log = LoggerFactory.getLogger(FilePlusUtil.class);

    public static final String DIR = "tmp";

    public static final String DATA_DIR = DIR + "/data";

    public static final String NODE_DATA_DIR = DIR + "/node";

    public static final String ASSET_DIR = DIR + "/asset";

    public static final String TEMPLATE_FILE_NAME = "template";

    public static final String WIDGET_PACKAGE_FILE_NAME = "widget_package";

    public static final String AUTOMATION_FILE_NAME = "automation";

    public static final String LAB_FEATURE_FILE_NAME = "lab_feature";

    public static final String WIZARD_CONFIG_FILE_NAME = "wizard_config";

    public static final String ASSET_INFO = "asset_info";

    public static final String CONFIG_DATASHEET_TEMPLATE_FILE_NAME = "config_datasheet_template";

    public static final String SHORT_LINE = "-";

    public static final String SLASH = "/";

    public static void clean() {
        FileUtil.del(DIR);
    }

    public static String writeAssetFileAndReturnContentType(String host, String relativePath, OssProperties.Signature signature) {
        return FilePlusUtil.writeAssetFileAndReturnContentType(host, relativePath, relativePath, signature);
    }

    public static String writeAssetFileAndReturnContentType(String host, String relativePath, String newRelativePath, OssProperties.Signature signature) {
        try {
            String urlStr = StrUtil.format("{}/{}", host, relativePath);
            Optional<OssProperties.Signature> optionalSignature = Optional.ofNullable(signature);
            if (optionalSignature.isPresent() && optionalSignature.get().isEnabled()) {
                // The Signature object is not empty and the status is open, requiring signature
                urlStr = getSignatureUrl(host, relativePath, signature.getEncryptKey(), Long.valueOf(signature.getExpireSecond()));
            }
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            byte[] bytes = IoUtil.readBytes(inputStream);
            String targetPath = StrUtil.isBlank(newRelativePath) ? relativePath : newRelativePath;
            FileUtil.writeBytes(bytes, StrUtil.format("{}/{}", ASSET_DIR, targetPath.replace(SLASH, SHORT_LINE)));
            return urlConnection.getContentType();
        }
        catch (IOException e) {
            log.error("Write asset[{}] failure!Message: {}", relativePath, e.getMessage());
        }
        return null;
    }

    public static String getSignatureUrl(String host, String fileName, String encryptKey, Long expires) {
        try {
            Date expireDate = Date.from(Instant.now().plusSeconds(expires));
            return CdnManager.createTimestampAntiLeechUrl(host, fileName, null,
                    encryptKey, DateUtil.toInstant(expireDate).getEpochSecond());
        } catch (QiniuException e) {
            log.error("get asset[{}] signatureUrl failure!Message: {}", fileName, e.getMessage());
        }
        return null;
    }

    public static String convertToRelativePath(String fileName) {
        return fileName.replace(SHORT_LINE, SLASH);
    }

    public static void writeNodeDataFile(String content) {
        FileUtil.writeUtf8String(Base64.encode(content), FilePlusUtil.getTemporaryFilePath(NODE_DATA_DIR, RandomExtendUtil.randomString(10)));
    }

    public static void writeDataFile(String content, String fileName) {
        FileUtil.writeUtf8String(Base64.encode(content), FilePlusUtil.getTemporaryFilePath(fileName));
    }

    public static String getTemporaryFilePath(String fileName) {
        return FilePlusUtil.getTemporaryFilePath(DATA_DIR, fileName);
    }

    private static String getTemporaryFilePath(String dir, String fileName) {
        return StrUtil.format("{}/{}", dir, fileName);
    }

    public static String parseFileContent(File file) {
        BufferedInputStream in = FileUtil.getInputStream(file);
        try {
            return FilePlusUtil.parseInputStream(in, true);
        }
        catch (IOException e) {
            throw new RuntimeException(StrUtil.format("Parse data file[{}] failure.Message: {}", file.getName(), e.getMessage()));
        }
    }

    public static String parseInputStream(InputStream in, boolean decode) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuilder content = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }
        return decode ? Base64.decodeStr(content.toString()) : content.toString();
    }
}
