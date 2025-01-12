/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory
 * and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { HttpModule } from '@nestjs/axios';
import { MiddlewareConsumer, Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CommandModule } from 'database/command/command.module';
import { DatabaseModule } from 'database/database.module';
import { DeveloperModule } from 'developer/developer.module';
import { EmbedDynamicModule } from 'embed/embed.dynamic.module';
import { FusionDashboardController } from 'enterprise/fusion/controllers/fusion.dashboard.controller';
import { FusionEmbedController } from 'enterprise/fusion/controllers/fusion.embed.controller';
import { FusionUnitController } from 'enterprise/fusion/controllers/fusion.unit.controller';
import { FusionViewController } from 'enterprise/fusion/controllers/fusion.view.controller';
import { FusionAiController } from 'enterprise/fusion/controllers/fusion.ai.controller';
import { FusionApiEnterpriseService } from 'enterprise/fusion/services/fusion-api.enterprise.service';
import { RestEnterpriseService } from 'enterprise/share/service/rest.enterprise.service';
import { ApiRequestMiddleware } from 'fusion/middleware/api.request.middleware';
import { ApiUsageRepository } from 'fusion/repositories/api.usage.repository';
import { DataBusService } from 'fusion/services/databus/databus.service';
import { NodeModule } from 'node/node.module';
import { NodeRateLimiterMiddleware } from 'shared/middleware/node.rate.limiter.middleware';
import { HttpConfigService } from 'shared/services/config/http.config.service';
import { UnitRepository } from 'unit/repositories/unit.repository';
import { UnitModule } from 'unit/unit.module';
import { UserModule } from 'user/user.module';
import { AiConversationService } from './services/ai.conversation.service';
import { AiConversationRepository } from './repositories/ai.conversation.repository';

@Module({
  controllers: [FusionDashboardController, FusionEmbedController, FusionViewController, FusionUnitController, FusionAiController],
  imports: [
    NodeModule,
    DatabaseModule,
    DeveloperModule,
    UserModule,
    UnitModule,
    EmbedDynamicModule.forRoot(),
    TypeOrmModule.forFeature([ApiUsageRepository, UnitRepository, AiConversationRepository]),
    CommandModule,
    HttpModule.registerAsync({
      useClass: HttpConfigService,
    }),
  ],
  providers: [DataBusService, FusionApiEnterpriseService, RestEnterpriseService, AiConversationService],
})
export class FusionApiEnterpriseModule {
  configure(consumer: MiddlewareConsumer) {
    consumer.apply(ApiRequestMiddleware, NodeRateLimiterMiddleware)
      .forRoutes(FusionDashboardController, FusionEmbedController, FusionViewController,
        FusionUnitController, FusionAiController);
  }
}
