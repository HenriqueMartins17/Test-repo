package com.apitable.enterprise.ai.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

/**
 * api operation.
 *
 * @author Shawn Deng
 */
public class ApiOperation {

    private static final Logger logger = LoggerFactory.getLogger(ApiOperation.class);

    protected static final ObjectMapper JSON_MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * request operation.
     *
     * @param uri       full url
     * @param respClazz response class object
     * @param <T>       vo
     * @return T response class
     * @throws AiServerException call api of ai-server error
     */
    protected static <T> T get(String uri, Class<T> respClazz) throws AiServerException {
        return execute(uri, HttpMethod.GET, null, respClazz);
    }

    /**
     * post operation.
     *
     * @param uri       uri
     * @param respClazz response class
     * @param <T>       response type
     * @return T
     * @throws AiServerException ai server exception
     */
    protected static <T> T post(String uri, Class<T> respClazz)
        throws AiServerException {
        return execute(uri, HttpMethod.POST, null, respClazz);
    }

    /**
     * post operation.
     *
     * @param uri       uri
     * @param body      body
     * @param respClazz response class
     * @param <T>       response type
     * @return T
     * @throws AiServerException ai server exception
     */
    protected static <T> T post(String uri, Object body, Class<T> respClazz)
        throws AiServerException {
        return execute(uri, HttpMethod.POST, body, respClazz);
    }

    private static <T> T execute(String uri, HttpMethod method, Object body, Class<T> respClazz) {
        RestClient restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .baseUrl(AiServer.getBaseUrl())
            .defaultStatusHandler(HttpStatusCode::isError,
                (request, response) -> {
                    throw new AiServerException(
                        String.format("fail to call ai server, statusCode: %s, message: %s",
                            response.getStatusCode().value(),
                            response.getStatusText()
                        ));
                })
            .build();

        String bodyAsString = restClient.method(method)
            .uri(uri)
            .body(body == null ? Collections.emptyMap() : body)
            .retrieve()
            .body(String.class);

        try {
            JsonNode bodyMap = JSON_MAPPER.readTree(bodyAsString);
            int code = bodyMap.get("code").asInt();
            if (code == 200) {
                JsonNode data = bodyMap.get("data");
                return JSON_MAPPER.convertValue(data, respClazz);
            } else if (code == 404) {
                // 404 not found
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new AiServerException("fail to parse data, internal server error", e);
        }
        return null;
    }

    protected static Flux<String> eventStream(String uri, String bodyAsString,
                                              ChatCallBack chatCallBack) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(30))
                .addHandlerLast(new WriteTimeoutHandler(30)));
        WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(AiServer.getBaseUrl())
            .build();

        return webClient.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(bodyAsString)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchangeToFlux(response -> {
                if (response.statusCode().isError()) {
                    return response.bodyToFlux(String.class)
                        .flatMap(errorBody -> {
                            logger.error("status code: {}, errorBody: {}", response.statusCode(),
                                errorBody);
                            return Flux.error(new AiServerException(errorBody));
                        });
                } else {
                    return response.bodyToFlux(String.class);
                }
            })
            .doOnCancel(chatCallBack::onCancel)
            .doOnError(chatCallBack::onError)
            .doOnComplete(chatCallBack::onComplete);
    }
}
