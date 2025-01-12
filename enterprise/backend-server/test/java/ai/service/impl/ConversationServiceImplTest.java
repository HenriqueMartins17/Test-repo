package com.apitable.enterprise.ai.service.impl;

import static com.apitable.enterprise.ai.server.Inference.getAiInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.model.Conversation;
import com.apitable.enterprise.ai.model.ConversationOrigin;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.util.page.PageInfo;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageRequest;

public class ConversationServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testUserPagination() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        // init conversation list
        String aiId = createAiNode(userSpace.getSpaceId());
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            inference.when(() -> getAiInfo(anyString())).thenReturn(null);
            createInternalConversation(userSpace.getUserId(), aiId, 2);
            createAnonymousConversation(aiId, 2);
        }

        // pagination test
        PageInfo<Conversation> firstPage =
            iAiConversationService.userPagination(userSpace.getUserId(), aiId,
                PageRequest.of(1, 1));
        assertThat(firstPage.getPageNum()).isEqualTo(1);
        assertThat(firstPage.getPageSize()).isEqualTo(1);
        assertThat(firstPage.getFirstPage()).isTrue();
        PageInfo<Conversation> lastPage = iAiConversationService.userPagination(
            userSpace.getUserId(), aiId, PageRequest.of(2, 1));
        assertThat(lastPage.getPageNum()).isEqualTo(2);
        assertThat(lastPage.getPageSize()).isEqualTo(1);
        assertThat(lastPage.getRecords()).hasSize(1);
        assertThat(lastPage.getLastPage()).isTrue();
    }

    @Test
    void testPagination() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        // init conversation list
        String aiId = createAiNode(userSpace.getSpaceId());
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            inference.when(() -> getAiInfo(anyString())).thenReturn(null);
            createInternalConversation(userSpace.getUserId(), aiId, 2);
            createAnonymousConversation(aiId, 2);
        }

        // pagination test
        PageInfo<Conversation> firstPage =
            iAiConversationService.pagination(aiId, PageRequest.of(1, 2));
        assertThat(firstPage.getPageNum()).isEqualTo(1);
        assertThat(firstPage.getPageSize()).isEqualTo(2);
        assertThat(firstPage.getFirstPage()).isTrue();
        PageInfo<Conversation> lastPage = iAiConversationService.pagination(
            aiId, PageRequest.of(2, 2));
        assertThat(lastPage.getPageNum()).isEqualTo(2);
        assertThat(lastPage.getPageSize()).isEqualTo(2);
        assertThat(lastPage.getRecords()).hasSize(2);
        assertThat(lastPage.getLastPage()).isTrue();
    }

    private void createInternalConversation(Long userId, String aiId, int nums) {
        for (int i = 0; i < nums; i++) {
            String title = "conversation-" + i;
            iAiConversationService.create(aiId, title, ConversationOrigin.INTERNAL, userId);
        }
    }

    private void createAnonymousConversation(String aiId, int nums) {
        for (int i = 0; i < nums; i++) {
            String title = "conversation-" + i;
            iAiConversationService.create(aiId, title, ConversationOrigin.ANONYMOUS, null);
        }
    }
}
