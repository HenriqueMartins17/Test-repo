/**
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

import { Module } from '@nestjs/common';
import { EmbedDynamicModule } from 'embed/embed.dynamic.module';
import { NodeModule } from 'node/node.module';
import { UnitModule } from 'unit/unit.module';
import { UserModule } from 'user/user.module';
import { HocuspocusBaseService } from 'workdoc/services/hocuspocus.base.service';
import { DocumentController } from './controller/document.controller';
import { DocumentEnterpriseModule } from './document.enterprise.module';
import { HocuspocusEnterpriseService } from './services/hocuspocus.enterprise.service';

@Module({
  imports: [
    UserModule,
    UnitModule,
    NodeModule,
    DocumentEnterpriseModule,
    EmbedDynamicModule.forRoot(),
  ],
  controllers: [
    DocumentController,
  ],
  providers: [
    {
      provide: HocuspocusBaseService,
      useClass: HocuspocusEnterpriseService
    },
  ],
  exports: [
    {
      provide: HocuspocusBaseService,
      useClass: HocuspocusEnterpriseService
    },
  ],
})
export class WorkDocEnterpriseModule {}
