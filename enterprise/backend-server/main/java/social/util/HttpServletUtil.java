package com.apitable.enterprise.social.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * http servlet util.
 */
public class HttpServletUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServletUtil.class);

    public static String getRequestBody(HttpServletRequest request) {
        try {
            return getRequestBody(request.getReader());
        } catch (IOException e) {
            // No need to deal with this problem
            LOG.error("get servlet request error", e);
            return null;
        }
    }

    public static String getRequestBody(BufferedReader reader) {
        final StringBuilder builder = new StringBuilder();
        // Default cache size: 8192
        final CharBuffer buffer = CharBuffer.allocate(2 << 12);
        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    public static void toResponseData(HttpServletResponse response, String responseText) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (Writer writer = response.getWriter()) {
            writer.write(responseText);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
