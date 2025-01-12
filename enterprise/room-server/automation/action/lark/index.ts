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
import { IActionResponse, IErrorResponse, ISuccessResponse } from 'automation/actions/interface/action.response';
import fetch from 'node-fetch';
interface ILarkMsgRequest {
  type: 'text';
  content: string;
  webhookUrl: string;
}

interface ILarkMsgSuccessResponse {
  Extra: any | null;
  StatusCode: number;
  StatusMessage: 'success' | string;
}

interface ILarkMsgFailResponse {
  code: number;
  msg: string;
}

type ILarkMsgResponse = ILarkMsgFailResponse | ILarkMsgSuccessResponse;

export async function sendLarkMsg(request: ILarkMsgRequest): Promise<IActionResponse<any>> {
  const { type, content, webhookUrl } = request;
  const body = JSON.stringify({
    msg_type: type,
    content: {
      [type]: content
    }
  });

  try {
    const resp = await fetch(webhookUrl.trim(), {
      body,
      method: 'POST',
      headers: {
        'content-type': 'application/json'
      },
    });
    const result: ILarkMsgResponse = await resp.json();
    if ((result as ILarkMsgSuccessResponse).StatusCode === 0) {
      const res: ISuccessResponse<any> = {
        data: result
      };
      return {
        success: true,
        data: res,
        code: ResponseStatusCodeEnums.Success
      };
    }
    
    const failRes = result as ILarkMsgFailResponse;
    const res: IErrorResponse = {
      errors: [{
        message: `[${failRes.code}] ${failRes.msg}`
      }]
    };
    // lark return result, but not success, means client input error or request too frequently, use lark response as error message.
    return {
      success: false,
      data: res,
      code: ResponseStatusCodeEnums.ClientError
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