import { Injectable } from '@nestjs/common';
import { IdWorker } from 'shared/helpers';
import { AiConversationRepository } from '../repositories/ai.conversation.repository';
import { aiServer } from '../helpers/ai.server.factory';
import { ConversationOrigin } from '../models/ai.enums';
import { AiConversationEntity } from '../entities/ai.conversation.entity';
import { generateConversationId } from '../helpers/util';

@Injectable()
export class AiConversationService {
  constructor(private readonly aiConversationRepository: AiConversationRepository) {}

  public async createConversation(userId: string, aiId: string, origin: ConversationOrigin = ConversationOrigin.Internal): Promise<string> {
    const conversationId = generateConversationId();
    // get latest success training id
    const aiInfo = await aiServer.getAiInfo(aiId);
    const latestSuccessTraining = aiInfo && aiInfo.latestSuccessTraining();
    const trainingId = latestSuccessTraining && latestSuccessTraining.id;
    const conversation = {
      id: IdWorker.nextId().toString(),
      aiId,
      trainingId,
      conversationId,
      title: 'conversation',
      origin,
      originType: origin,
      createdBy: userId,
      updatedBy: userId,
    };
    await this.aiConversationRepository.save(conversation);
    return conversationId;
  }

  public async getLastConversationByAiIdAndCreatedBy(aiId: string, createdBy: string): Promise<AiConversationEntity | undefined> {
    return await this.aiConversationRepository.selectLastOneByAiIdAndCreatedBy(aiId, createdBy);
  }

  public async checkConversation(conversationId: string): Promise<void> {
    const conversation = await this.aiConversationRepository.selectByConversationId(conversationId);
    if (!conversation) {
      throw new Error('Conversation not found');
    }
  }
}
