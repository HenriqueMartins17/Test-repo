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

import { Body, Controller, Param, Post, Res, UseGuards, UseInterceptors } from '@nestjs/common';
import { ApiBearerAuth, ApiConsumes, ApiOkResponse, ApiOperation, ApiProduces, ApiTags } from '@nestjs/swagger';
import { EnterpriseGuard } from 'enterprise/share/guards/enterprise.guard';
import type { FastifyReply } from 'fastify';
import { ApiAuthGuard } from 'fusion/middleware/guard/api.auth.guard';
import { ApiUsageGuard } from 'fusion/middleware/guard/api.usage.guard';
import { ParseObjectPipe } from 'fusion/middleware/pipe/parse.pipe';
import { SwaggerConstants } from 'shared/common';
import { ApiUsageInterceptor } from 'shared/interceptor/api.usage.interceptor';
import { AiChatCompletionParamRo, AiChatCompletionRo, AiChatCompletionVo, INestChatOptions } from '../models/ai.model';
import { aiServer } from '../helpers/ai.server.factory';
import { AiConversationService } from '../services/ai.conversation.service';
import { ConversationOrigin } from '../models/ai.enums';

@ApiTags(SwaggerConstants.ENTERPRISE_TAG)
@Controller('fusion/v1')
@ApiBearerAuth()
@UseGuards(ApiAuthGuard, ApiUsageGuard, EnterpriseGuard)
@UseInterceptors(ApiUsageInterceptor)
export class FusionAiController {

  constructor(
    private readonly conversationService: AiConversationService
  ) {}

  @Post('ai/:aiId/chat/completions')
  @ApiOperation({
    summary: 'Creates a model response for the given chat conversation',
  })
  @ApiProduces('application/json','text/event-stream')
  @ApiConsumes('application/json')
  @ApiOkResponse({
    description: 'Returns a chat completion object',
    type: AiChatCompletionVo
  })
  public async createChatCompletion(@Param() param: AiChatCompletionParamRo,
                                    @Body(new ParseObjectPipe()) body: AiChatCompletionRo,
                                    @Res() res: FastifyReply){
    const conversationId = await this.conversationService.createConversation('', param.aiId, ConversationOrigin.Anonymous);
    const contentType = body.stream ? 'text/event-stream' : 'application/json';
    const headers = {
      'Content-Type': contentType,
      Connection: 'keep-alive',
      'Cache-Control': 'no-cache',
      'X-Conversation-Id': conversationId,
    };
    res.raw.writeHead(200, headers);

    const chatOptions = new INestChatOptions(param.aiId,conversationId,body);
    chatOptions.onError = (err: Error) => {
      res.raw.write(err.message);
      res.raw.end();
    };
    chatOptions.onUpdate = (message) => {
      res.raw.write(message);
    };
    chatOptions.onFinish = () => {
      res.raw.end();
    };
    aiServer.chatCompletionsStream(chatOptions);
  }
}
