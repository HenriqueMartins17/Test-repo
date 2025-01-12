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

import { Injectable } from '@nestjs/common';
import { customActionTypeMetas } from 'automation/actions/decorators/automation.action.decorator';
import { AutomationActionTypeRepository } from 'automation/repositories/automation.action.type.repository';
import { AutomationServiceRepository } from 'automation/repositories/automation.service.repository';
import { RobotActionTypeBaseService } from 'automation/services/robot.action.type.base.service';
import { getTypeByItem } from 'automation/utils/i18n.util';
import { IActionTypeDetailVo } from 'automation/vos/action.type.detail.vo';

@Injectable()
export class RobotActionTypeEnterpriseService extends RobotActionTypeBaseService {

  constructor(
    private automationActionTypeRepository: AutomationActionTypeRepository,
    private automationServiceRepository: AutomationServiceRepository) {
    super();
  }

  override async getActionType(lang = 'zh'): Promise<IActionTypeDetailVo[]> {
    const actionTypes = await this.automationActionTypeRepository.find({ where: { isDeleted: 0 }});
    const result = [];
    for (const actionTypesKey in actionTypes) {
      const actionType = actionTypes[actionTypesKey];
      const service = await this.automationServiceRepository.findOne({
        where: { serviceId: actionType?.serviceId }
      });
      const actionTypeDetailVo = getTypeByItem({
        actionTypeId: actionType?.actionTypeId,
        name: actionType?.name,
        description: actionType?.description,
        endpoint: actionType?.endpoint,
        i18n: actionType?.i18n,
        inputJsonSchema: actionType?.inputJSONSchema,
        outputJsonSchema: actionType?.outputJSONSchema,
        serviceId: service?.serviceId,
        serviceName: service?.name,
        serviceLogo: service?.logo,
        serviceSlug: service?.slug,
        serviceI18n: service?.i18n,
      }, lang) as IActionTypeDetailVo;
      result.push(actionTypeDetailVo);
    }
    result.push(...customActionTypeMetas.values());
    return result;
  }
}