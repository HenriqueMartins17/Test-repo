import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { NodeModule } from 'node/node.module';
import { UserModule } from 'user/user.module';
import { AiController } from './controllers/ai.controller';
import { UnitModule } from 'unit/unit.module';
import {
  AiService,
} from './services';
import {
  AiRepository
} from './repositories';
import { FormService } from 'database/form/services/form.service';
import { DatabaseModule } from 'database/database.module';
import { FusionApiTransformer } from 'fusion/transformer/fusion.api.transformer';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      AiRepository,
    ]),
    NodeModule,
    UserModule,
    UnitModule,
    DatabaseModule,
  ],
  controllers: [AiController],
  providers: [
    AiService,
    FormService,
    FusionApiTransformer,
  ],
  exports: [
    AiService,
  ]
})
export class AiEnterpriseModule {
}
