package com.apitable.enterprise.ai.server;

import static com.apitable.enterprise.ai.server.Inference.sendChatCompletions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.time.Duration;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class InferenceTest {

    @Test
    void testSendChatCompletions() {

        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            List<String> body = Lists.list(
                "data: {\"id\": \"aitable_mock_1695123276\", \"conversation_id\": null, \"actions\": null, \"object\": \"chat.completion.chunk\", \"created\": 1695123276, \"model\": \"gpt-3.5-turbo\", \"choices\": [{\"index\": 0, \"delta\": {\"role\": \"assistant\", \"content\": \"I\"}, \"finish_reason\": null}]}",
                "data: {\"id\": \"aitable_mock_1695123276\", \"conversation_id\": null, \"actions\": null, \"object\": \"chat.completion.chunk\", \"created\": 1695123276, \"model\": \"gpt-3.5-turbo\", \"choices\": [{\"index\": 0, \"delta\": {\"role\": \"assistant\", \"content\": \" don\"}, \"finish_reason\": null}]}",
                "data: {\"id\": \"aitable_mock_1695123276\", \"conversation_id\": null, \"actions\": null, \"object\": \"chat.completion.chunk\", \"created\": 1695123276, \"model\": \"gpt-3.5-turbo\", \"choices\": [{\"index\": 0, \"delta\": {\"role\": \"assistant\", \"content\": \"'t\"}, \"finish_reason\": null}]}",
                "data: {\"id\": \"aitable_mock_1695123276\", \"conversation_id\": null, \"actions\": null, \"object\": \"chat.completion.chunk\", \"created\": 1695123276, \"model\": \"gpt-3.5-turbo\", \"choices\": [{\"index\": 0, \"delta\": {\"role\": \"assistant\", \"content\": \" know\"}, \"finish_reason\": null}]}",
                "data: {\"id\": \"aitable_mock_1695123276\", \"conversation_id\": null, \"actions\": null, \"object\": \"chat.completion.chunk\", \"created\": 1695123276, \"model\": \"gpt-3.5-turbo\", \"choices\": [{\"index\": 0, \"delta\": {\"role\": \"assistant\", \"content\": \".\"}, \"finish_reason\": null}]}",
                "data: [DONE]"
            );

            ChatCallBack callBack = new ChatCallBack() {
                @Override
                public void onCancel() {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            };
            inference.when(() -> sendChatCompletions("mock", "mock", "hello", callBack))
                .thenReturn(Flux.fromIterable(body));
            Flux<String> result = sendChatCompletions("mock", "mock", "hello", callBack);

            StepVerifier.create(result)
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isNotNull();
                })
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isNotNull();
                })
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isNotNull();
                })
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isNotNull();
                })
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isNotNull();
                })
                .consumeNextWith(p -> {
                    System.out.println(p);
                    assertThat(p).isEqualTo("data: [DONE]");
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
        }
    }
}
