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

import { ApiTipConstant } from '@apitable/core';
import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { InternalSpaceSubscriptionView } from 'database/interfaces';
import { EmbedLinkService } from 'enterprise/embed/services/embedlink.service';
import { ApiException } from 'shared/exception';
import { RestService } from 'shared/services/rest/rest.service';

@Injectable()
export class EmbedGuard implements CanActivate {
  constructor(private readonly restService: RestService, private readonly embedLinkService: EmbedLinkService) {
  }

  public async canActivate(context: ExecutionContext): Promise<boolean> {
    const req = context.switchToHttp().getRequest();
    let spaceId = req.params.spaceId;
    // embed
    if (!spaceId && req.params.linkId) {
      spaceId = await this.embedLinkService.getSpaceIdByLinkId(req.params.linkId);
    }
    if (!spaceId) {
      return true;
    }
    const spaceSubscription: InternalSpaceSubscriptionView = await this.restService.getSpaceSubscription(spaceId);
    if (!spaceSubscription.allowEmbed) {
      throw ApiException.tipError(ApiTipConstant.api_enterprise_limit);
    }
    return spaceSubscription.allowEmbed;
  }
}
