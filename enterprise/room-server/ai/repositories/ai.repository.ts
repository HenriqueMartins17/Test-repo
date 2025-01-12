import { EntityRepository, Repository } from 'typeorm';
import { AiEntity } from '../entities';

@EntityRepository(AiEntity)
export class AiRepository extends Repository<AiEntity> {
  selectByAiId(aiId: string): Promise<AiEntity | undefined> {
    return this.findOne({
      where: {
        aiId,
        isDeleted: 0,
      },
    });
  }
}
