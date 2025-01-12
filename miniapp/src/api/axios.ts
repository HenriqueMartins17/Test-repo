import axios, { AxiosRequestConfig, AxiosResponse } from 'taro-axios';
import Taro from '@tarojs/taro';
import { apiUrl } from '../config/index';
export const instance = axios.create({
  baseURL: apiUrl,
  // baseURL: 'https://vbs-integration.vika.ltd/api/v1/',
});

export interface IResponseData {
  success: boolean;
  code: number;
  message: string;
  data: any;
}

instance.interceptors.request.use(function (config: AxiosRequestConfig) {
  if (Taro.getStorageSync('session')) {
    const xsrf = JSON.parse(Taro.getStorageSync('xsrf'));
    const _xsrf = xsrf.replace(',', ';');
    config.headers['cookie'] = _xsrf + JSON.parse(Taro.getStorageSync('session'));
  }
  return config;
}, function (error: any) {
  return Promise.reject(error);
});

instance.interceptors.response.use((response: AxiosResponse<IResponseData>) => {
  if (response.headers['Set-Cookie'] && /\/authorize/.test(response.config.url!)) {
    // wechat/miniapp/authorize 接口会返回
    const cookie = response.headers['Set-Cookie'];
    if (/([\s\S]*)?(SESSION[\s\S]*)/.test(cookie)) {
      const xsrf = RegExp.$1;
      const session = RegExp.$2;
      if (xsrf) {
        Taro.setStorageSync('xsrf', JSON.stringify(xsrf));
      }
      if (session) {
        Taro.setStorageSync('session', JSON.stringify(session));
      }
    }
  }
  if (response.data.success) {
    return Promise.resolve(response.data.data);
  }
  return Promise.reject(response.data);
}, function (error: any) {
  return Promise.reject(error);
});

