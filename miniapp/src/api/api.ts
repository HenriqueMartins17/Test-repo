import { instance as axios } from './index';
import { IResponseData } from './axios';
import { IMyInfo, IBindWechatCode } from './interface';


export const bindWechatCode = code => {
  return axios.get<IBindWechatCode, IBindWechatCode>('wechat/miniapp/authorize', {
    params: {
      code,
    },
  });
};

export const getLogin = data => {
  return axios.post<Pick<IResponseData, 'data'>>('auth/signIn', data);
};

export const bindUserPhone = (data: { encryptedData: string, iv: string }) => {
  return axios.get<Pick<IResponseData, 'data'>>('wechat/miniapp/phone', {
    params: data,
  });
};

export const getCmsCode = (phone: string, type: number) => {
  return axios.post<Pick<IResponseData, 'data'>>('base/action/sms/code', {
    phone,
    type,
  });
};

/**
 * 和二维码相关的操作
 * mark 二维码上带的识别id
 * type:
 * 0：验证二维码是否在有效期
 * 1：确定授权登录
 * 2：取消授权/绑定
 * 3：确定绑定
 *
 */
export const qrCodeOperate = (mark: string, type: number) => {
  return axios.get('wechat/miniapp/operate', {
    params: {
      mark,
      type,
    },
  });
};

export const getVikaUserInfo = () => {
  return axios.get<IMyInfo, IMyInfo>('wechat/miniapp/getInfo');
};
