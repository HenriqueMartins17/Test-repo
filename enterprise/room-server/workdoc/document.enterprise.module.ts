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

import { Module, forwardRef } from '@nestjs/common';
import { Configuration } from 'enterprise/share/databus_client/configuration';
import { DatabusEnterpriseApiModule } from 'enterprise/share/databus_client/databus.enterprise.api.module';
import { GrpcModule } from 'grpc/grpc.module';
import { DocumentBaseService } from 'workdoc/services/document.base.service';
import { DocumentEventExecutor } from './event/document.event.executor';
import { DocumentService } from './services/document.service';

@Module({
  imports: [
    DatabusEnterpriseApiModule.forRoot(() => {
      return new Configuration({basePath: process.env.DATABUS_SERVER_BASE_URL});
    }),
    forwardRef(() => GrpcModule),
  ],
  providers: [
    {
      provide: DocumentBaseService,
      useClass: DocumentService
    },
    DocumentEventExecutor,
    DocumentService,
  ],
  exports: [
    {
      provide: DocumentBaseService,
      useClass: DocumentService
    },
    DocumentService,
  ],
})
export class DocumentEnterpriseModule {}
