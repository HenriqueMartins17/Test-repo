import { Injectable } from '@nestjs/common';
import { AIType, QaSetting } from '../models';
import { AiRepository } from '../repositories';
import { NodeService } from 'node/services/node.service';
import { UnitMemberService } from 'unit/services/unit.member.service';
import { isEmpty } from 'lodash';
import { NodeShareSettingService } from 'node/services/node.share.setting.service';
import { UserService } from 'user/services/user.service';
import { NodePermissionService } from 'node/services/node.permission.service';
import { PermissionException, ServerException } from 'shared/exception';

@Injectable()
export class AiService {
  constructor(
    private readonly aiRepository: AiRepository,
    private readonly userService: UserService,
    private readonly nodeService: NodeService,
    private readonly nodePermissionService: NodePermissionService,
    private readonly memberService: UnitMemberService,
    private readonly nodeShareSettingService: NodeShareSettingService,
  ) {}

  /**
   * get ai entity by ai id.
   * @param aiId ai id
   */
  public async getByAiId(aiId: string) {
    return await this.aiRepository.selectByAiId(aiId);
  }

  public async checkAiNodeAccess(aiId: string, auth: { cookie: string }): Promise<{ userId: string }> {
    const { userId } = await this.userService.getMeNullable(auth.cookie);
    if (userId) {
      // Get the space ID which the node belongs to
      const spaceId = await this.nodeService.getSpaceIdByNodeId(aiId);
      const memberId = await this.memberService.getIdBySpaceIdAndUserId(spaceId, userId);
      if (isEmpty(memberId)) {
        // check ai node whether enable shared
        const isShared = await this.nodeShareSettingService.getNodeShareStatus(aiId);
        if (!isShared) {
          throw new ServerException(PermissionException.ACCESS_DENIED);
        }
      } else {
        // look up the member's permission
        const permission = await this.nodePermissionService.getNodeRole(aiId, auth);
        if (!permission?.readable) {
          // check ai node whether enable shared
          const isShared = await this.nodeShareSettingService.getNodeShareStatus(aiId);
          if (!isShared) {
            throw new ServerException(PermissionException.ACCESS_DENIED);
          }
        }
      }
    } else {
      // check whether ai node enable shared
      // await this.nodeShareSettingService.checkNodeShareStatus(aiId);
    }
    return { userId };
  }

  /**
   * get ai form id in setting.
   * @param aiId ai id
   */
  public async getFormIdFromSetting(aiId: string): Promise<string | undefined> {
    const aiEntity = await this.getByAiId(aiId);
    if (!aiEntity) {
      throw new Error('Failed to find ai node');
    }
    if (aiEntity.type === AIType.QA) {
      const setting = aiEntity.setting as QaSetting;
      return setting?.formId;
    }
    return undefined;
  }
}
