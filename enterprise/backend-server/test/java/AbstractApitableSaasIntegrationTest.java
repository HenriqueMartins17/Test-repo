/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise;

import static com.apitable.enterprise.apitablebilling.enums.ProductEnum.BUSINESS;
import static com.apitable.enterprise.apitablebilling.enums.ProductEnum.PLUS;
import static com.apitable.enterprise.apitablebilling.enums.ProductEnum.PRO;
import static com.apitable.enterprise.apitablebilling.enums.ProductEnum.STARTER;
import static com.apitable.shared.util.IdUtil.createNodeId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.apitable.AbstractIntegrationTest;
import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.model.AbstractAiNode;
import com.apitable.enterprise.ai.model.AiDatasheetObject;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.queue.TrainingQueueConsumer;
import com.apitable.enterprise.ai.scheduler.CreditSummaryTask;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiCreditService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionOverallService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.enterprise.ai.service.IAiNodeService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.ai.service.IAiTrainingDataSourceService;
import com.apitable.enterprise.ai.service.IAiTrainingService;
import com.apitable.enterprise.airagent.entity.AgentUserEntity;
import com.apitable.enterprise.airagent.service.IAgentAuthService;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.enterprise.airagent.service.IAgentUserService;
import com.apitable.enterprise.airagent.service.IDataSourceService;
import com.apitable.enterprise.apitablebilling.enums.BillingPeriod;
import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import com.apitable.enterprise.apitablebilling.enums.RecurringInterval;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.enterprise.apitablebilling.model.dto.EntitlementCreationDTO;
import com.apitable.enterprise.apitablebilling.rewardful.RewardfulService;
import com.apitable.enterprise.apitablebilling.service.IBillingService;
import com.apitable.enterprise.apitablebilling.service.IEntitlementService;
import com.apitable.enterprise.apitablebilling.service.IStripeEventHandler;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionInApitableService;
import com.apitable.enterprise.auth0.autoconfigure.Auth0Template;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.enterprise.auth0.service.IUserBindService;
import com.apitable.enterprise.stripe.config.Price;
import com.apitable.enterprise.stripe.config.ProductCatalogFactory;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.interfaces.ai.model.AiType;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.config.initializers.EnterpriseEnvironmentInitializers;
import com.apitable.workspace.dto.DatasheetSnapshot;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.ro.NodeOpRo;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = {
    EnterpriseEnvironmentInitializers.class,
    TestApitableSaasContextInitializer.class
})
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
    classes = {
        TrainingQueueConsumer.class,
    })
)
public abstract class AbstractApitableSaasIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected IAiService iAiService;

    @Autowired
    protected IAiTrainingService iAiTrainingService;

    @Autowired
    protected IAiNodeService iAiNodeService;

    @Autowired
    protected IAiTrainingDataSourceService iAiTrainingDataSourceService;

    @Autowired
    protected IAiConversationService iAiConversationService;

    @Autowired
    protected IAiCreditTransactionService iAiCreditTransactionService;

    @Autowired
    protected IAiCreditTransactionOverallService iAiCreditTransactionOverallService;

    @Autowired
    protected IAiCreditService iAiCreditService;

    @Autowired
    protected IEntitlementService iEntitlementService;

    @Autowired
    protected IBillingService iBillingService;

    @MockBean
    protected StripeTemplate stripeTemplate;

    @MockBean
    protected EntitlementServiceFacade entitlementServiceFacade;

    @Autowired
    protected Auth0Service auth0Service;

    @MockBean
    protected Auth0Template auth0Template;

    @Autowired
    protected CreditSummaryTask creditSummaryTask;

    @Autowired
    protected RewardfulService rewardfulService;

    @Autowired
    protected IAgentAuthService iAgentAuthService;

    @Autowired
    protected IAgentService iAgentService;

    @Autowired
    protected IAgentUserService iAgentUserService;

    @Autowired
    protected IDataSourceService iDataSourceService;

    @Autowired
    protected IStripeEventHandler iStripeEventHandler;

    @Resource
    protected IUserBindService iUserBindService;

    @Resource
    protected ISubscriptionInApitableService iSubscriptionInApitableService;

    protected static final String DEFAULT_AI_NODE_NAME = "ChatBot Assistant";

    protected Price findPrice(ProductEnum product, RecurringInterval interval) {
        assertThat(product).isIn(Arrays.asList(STARTER, PLUS, PRO, BUSINESS));
        Optional<Price> priceOptional =
            ProductCatalogFactory.findProduct(product, true)
                .getPrices().stream()
                .filter(price -> price.getInterval().equals(interval.getName()))
                .findFirst();
        if (priceOptional.isPresent()) {
            return priceOptional.get();
        }
        throw new RuntimeException("no exist specifically price");
    }

    protected MockUserSpace createEntitlement(ProductEnum product, String priceId,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate) {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iEntitlementService.createEntitlement(EntitlementCreationDTO.builder()
            .spaceId(userSpace.getSpaceId())
            .productName(product.getName())
            .priceId(priceId)
            .quantity(1)
            .period(BillingPeriod.MONTHLY)
            .startDate(startDate)
            .endDate(endDate)
            .build()
        );
        return userSpace;
    }

    protected String createAiNode(String spaceId) {
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(spaceId);
        aiObject.setAiId(aiNodeId);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        iAiService.create(aiObject);
        return aiNodeId;
    }

    protected String createAiNodeWithQaType() {
        MockData mockData = createMockData();
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(mockData.getUserSpace().getSpaceId());
        aiObject.setAiId(aiNodeId);
        aiObject.setType(AiType.QA);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        iAiService.create(aiObject);

        AiDatasheetObject obj = new AiDatasheetObject();
        obj.setId(mockData.getDatasheetId());
        obj.setType(NodeType.DATASHEET);
        obj.setRows(100);
        DatasheetSnapshot.Field field = new DatasheetSnapshot.Field();
        field.setId("fld_xxxxx");
        obj.setFields(Collections.singletonList(field));
        createAiNodeBatch(aiNodeId, Collections.singletonList(obj));
        return aiNodeId;
    }

    public <T extends AbstractAiNode> void createAiNodeBatch(String aiId, List<T> nodeObjects) {
        if (nodeObjects == null || nodeObjects.isEmpty()) {
            return;
        }
        List<AiNodeEntity> aiNodeEntities = new ArrayList<>();
        nodeObjects.forEach(nodeObject -> {
            AiNodeEntity aiNodeEntity = new AiNodeEntity();
            aiNodeEntity.setAiId(aiId);
            aiNodeEntity.setNodeId(nodeObject.getId());
            aiNodeEntity.setNodeType(nodeObject.getType().getNodeType());
            aiNodeEntity.setSetting(nodeObject.toJson());
            aiNodeEntities.add(aiNodeEntity);
        });
        iAiNodeService.saveBatch(aiNodeEntities);
    }

    protected MockData createMockData() {
        MockData mockData = new MockData();
        mockData.userSpace = createSingleUserAndSpace();
        mockData.rootNodeId = iNodeService.getRootNodeIdBySpaceId(mockData.userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(mockData.userSpace.getSpaceId());
        mockData.datasheetId =
            iNodeService.createNode(mockData.userSpace.getUserId(), mockData.userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(mockData.rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        DatasheetSnapshot snapshot = iDatasheetMetaService.getMetaByDstId(mockData.datasheetId);
        // extract first view id
        DatasheetSnapshot.View view = snapshot.getMeta().getViews().iterator().next();
        mockData.viewId = view.getId();
        return mockData;
    }

    protected AgentUserContext createAgentUser() {
        AgentUserEntity user = new AgentUserEntity();
        user.setEmail("test_user@agent.ai");
        iAgentUserService.save(user);

        // init user context
        initCallContext(user.getId());

        return new AgentUserContext(user.getId());
    }

    @Getter
    protected static class AgentUserContext {

        private final Long userId;

        public AgentUserContext(Long userId) {
            this.userId = userId;
        }

    }

    public static class MockData {

        private MockUserSpace userSpace;

        private String rootNodeId;

        private String datasheetId;
        private String viewId;

        public MockUserSpace getUserSpace() {
            return userSpace;
        }

        public String getRootNodeId() {
            return rootNodeId;
        }

        public String getDatasheetId() {
            return datasheetId;
        }

        public String getViewId() {
            return viewId;
        }
    }
}
