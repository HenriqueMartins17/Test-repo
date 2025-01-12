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
import { TypeOrmModule } from '@nestjs/typeorm';
import { DatabaseModule } from 'database/database.module';
import { FormService } from 'database/form/services/form.service';
import { EmbedlinkController } from 'enterprise/embed/controllers/embedlink.controller';
import { EmbedLinkRepository } from 'enterprise/embed/repositories/embedlink.repository';
import { FusionApiTransformer } from 'fusion/transformer/fusion.api.transformer';
import { NodeModule } from 'node/node.module';
import { UnitModule } from 'unit/unit.module';
import { UserModule } from 'user/user.module';
import { EmbedLinkService } from './services/embedlink.service';

@Module({
  imports: [NodeModule, UnitModule, UserModule, DatabaseModule, TypeOrmModule.forFeature([EmbedLinkRepository])],
  controllers: [EmbedlinkController],
  providers: [EmbedLinkService, FormService, FusionApiTransformer],
  exports: [EmbedLinkService],
})
export class EmbedEnterpriseModule {
}
