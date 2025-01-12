package com.apitable.enterprise.auth0.util;

import com.apitable.workspace.model.Datasheet;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.io.IOException;
import java.io.InputStream;

/**
 * qa records loader.
 *
 * @author Shawn Deng
 */
public class QaExampleLoader {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Datasheet get() {
        return QaExampleLoader.Singleton.INSTANCE.getSingleton();
    }

    private enum Singleton {
        INSTANCE;

        private final Datasheet singleton;

        Singleton() {
            try {
                InputStream resourceAsStream = QaExampleLoader.class.getResourceAsStream(
                    "/enterprise/config/qa.json");
                if (resourceAsStream == null) {
                    throw new IOException("example config file not found!");
                }
                singleton = mapper.readValue(resourceAsStream, Datasheet.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load system configuration!", e);
            }
        }

        public Datasheet getSingleton() {
            return singleton;
        }
    }
}
