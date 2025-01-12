import { EntityRepository, Repository } from 'typeorm';
import { AiConversationEntity } from '../entities/ai.conversation.entity';
import { ConversationOrigin } from '../models/ai.enums';

@EntityRepository(AiConversationEntity)
export class AiConversationRepository extends Repository<AiConversationEntity> {
  selectByAiId(aiId: string): Promise<AiConversationEntity[]> {
    return this.find({ where: { aiId, isDeleted: false } });
  }

  selectByConversationId(conversationId: string): Promise<AiConversationEntity | undefined> {
    return this.findOne({ where: { conversationId, isDeleted: false } });
  }

  selectLastOneByAiIdAndCreatedBy(aiId: string, createdBy: string): Promise<AiConversationEntity | undefined> {
    return this.findOne({
      where: {
        aiId,
        origin: ConversationOrigin.Internal,
        isDeleted: false,
        createdBy,
      },
      order: { createdAt: 'DESC' },
    });
  }
}
