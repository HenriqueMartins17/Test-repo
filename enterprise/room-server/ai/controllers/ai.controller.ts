import { Body, Controller, Get, Headers, Param, Post } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import { SwaggerConstants } from 'shared/common';
import { AiService } from '../services';
import { FormDataPack } from 'database/interfaces';
import { FormService } from 'database/form/services/form.service';
import { IRecordCellValue } from '@apitable/core';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('nest/v1')
export class AiController {
  constructor(
    private readonly aiService: AiService,
    private readonly formService: FormService,
  ) {
  }

  @Get('ai/:aiId/form')
  async getAiFormMeta(@Headers('cookie') cookie: string,
                    @Param('aiId') aiId: string): Promise<FormDataPack> {
    const { userId } = await this.aiService.checkAiNodeAccess(aiId, { cookie });
    const formId = await this.aiService.getFormIdFromSetting(aiId);
    if (!formId) {
      // don't have form
      throw new Error('Failed to find form id in setting');
    }
    return await this.formService.fetchFormData(formId, userId,{ cookie });
  }

  @Post('ai/:aiId/form')
  async addAiFormRecord(@Headers('cookie') cookie: string,
                      @Param('aiId') aiId: string,
                      @Body() recordData: IRecordCellValue): Promise<void> {
    const { userId } = await this.aiService.checkAiNodeAccess(aiId, { cookie });
    const formId = await this.aiService.getFormIdFromSetting(aiId);
    if (!formId) {
      // don't have form
      throw new Error('Failed to find form id in setting');
    }
    return await this.formService.addFormRecord({ formId, userId, recordData }, { cookie });
  }
}
