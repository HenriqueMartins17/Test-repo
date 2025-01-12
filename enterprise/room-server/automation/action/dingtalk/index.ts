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

import { ResponseStatusCodeEnums } from 'automation/actions/enum/response.status.code.enums';
import { IActionResponse, IErrorResponse } from 'automation/actions/interface/action.response';
import fetch from 'node-fetch';

interface IDingTalkMsgRequest {
  type: string;
  content: string;
  webhookUrl: string;
  title?: string;
}

interface IDingTalkMsgResponse {
  errmsg: string,
  errcode: number
}

export async function sendDingtalkMsg(request: IDingTalkMsgRequest): Promise<IActionResponse<any>> {
  const { type, content, webhookUrl, title } = request;
  let data = {};
  switch (type) {
    case 'text':
      data = {
        text: {
          content: content
        },
        msgtype: 'text'
      };
      break;
    case 'markdown':
      data = {
        msgtype: 'markdown',
        markdown: {
          title: title,
          text: content
        }
      };
      break;
  }
  try {
    const res = await fetch(webhookUrl, {
      body: JSON.stringify(data),
      method: 'POST',
      headers: {
        'content-type': 'application/json'
      },
    });
    const resp: IDingTalkMsgResponse = await res.json();
    if (resp.errcode === 0) {
      return {
        success: true,
        code: ResponseStatusCodeEnums.Success,
        data: {
          data: resp
        }
      };
    }
    return {
      success: false,
      code: ResponseStatusCodeEnums.ClientError,
      data: {
        errors: [{
          message: resp.errmsg
        }]
      }
    };
  } catch (error: any) {
    // network error
    const res: IErrorResponse = {
      errors: [{
        message: error.message
      }]
    };
    return {
      success: false,
      data: res,
      code: ResponseStatusCodeEnums.ServerError
    };
  }
}