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

// APIs for enterprise edition

import axios from 'axios';
import * as Url from './url.enterprise';
import * as UrlBilling from './url.billing';
import urlcat from 'urlcat';
import { ISocialWecomGetConfigResponse, IWecomAgentBindSpaceResponse, ISyncMemberRequest } from './api.enterprise.interface';
import { IApiWrapper, QrAction } from '../../../exports/store';

import {
  ICreateOrderResponse,
  IPayOrderResponse,
  IQueryOrderDiscountResponse,
  IQueryOrderPriceResponse,
  IQueryOrderStatusResponse,
} from '../../shared/api/api.interface';

// ***************************** lark

/**
 * get lark user info
 * @param token
 * @returns
 */
export function socialFeiShuUserAuth(token: string) {
  return axios.get(Url.SOCIAL_FEISHU_USER_AUTH, {
    params: { access_token: token },
  });
}

/**
 * whether or not the current lark user is the lark app admin
 * @param openId
 * @param tenantKey
 * @returns
 */
export function socialFeiShuCheckAdmin(openId: string, tenantKey: string) {
  return axios.get(Url.SOCIAL_FEISHU_CHECK_ADMIN, {
    params: { openId, tenantKey },
  });
}

/**
 * check lark tenant whether or not bind space
 *
 * @param tenantKey
 * @returns
 */
export function socialFeiShuCheckTenantBind(tenantKey: string) {
  return axios.get(Url.SOCIAL_FEISHU_CHECK_TENANT_BIND, {
    params: { tenantKey },
  });
}

/**
 * update lark base config
 * @param appInstanceId
 * @param appKey
 * @param appSecret
 * @returns
 */
export function updateLarkBaseConfig(appInstanceId: string, appKey: string, appSecret: string) {
  return axios.put(urlcat(Url.UPDATE_LARK_BASE_CONFIG, { appInstanceId }), { appKey, appSecret });
}

/**
 *
 * update lark event config
 *
 * @param appInstanceId
 * @param eventEncryptKey
 * @param eventVerificationToken
 * @returns
 */
export function updateLarkEventConfig(appInstanceId: string, eventEncryptKey: string, eventVerificationToken: string) {
  return axios.put(urlcat(Url.UPDATE_LARK_EVENT_CONFIG, { appInstanceId }), { eventEncryptKey, eventVerificationToken });
}

/**
 * bind user with lark account
 * @param areaCode
 * @param mobile
 * @param code
 * @param openId
 * @param tenantKey
 * @returns
 */
export function socialFeiShuBindUser(areaCode: string, mobile: string, code: string, openId: string, tenantKey: string) {
  return axios.post(Url.SOCIAL_FEISHU_BIND_USER, {
    areaCode,
    openId,
    tenantKey,
    mobile,
    code,
  });
}

/**
 * binding space with lark tenant app
 *
 * @param tenantKey
 * @param spaceList
 * @returns
 */
export function socialFeiShuBindSpace(tenantKey: string, spaceList: any[]) {
  return axios.post(urlcat(Url.SOCIAL_FEISHU_BIND_SPACE, { tenantKey }), spaceList);
}

/**
 * get lark organization tenant info
 * @param tenantKey
 * @returns
 */
export function getFeiShuTenantInfo(tenantKey: string) {
  return axios.get(urlcat(Url.SOCIAL_FEISHU_TENANT_INFO, { tenantKey }));
}

/**
 * lark user login
 * @param openId
 * @param tenantKey
 * @returns
 */
export function feishuUserLogin(openId: string, tenantKey: string) {
  return axios.post(Url.SOCIAL_FEISHU_USER_LOGIN, { openId, tenantKey });
}

/**
 * get lark tenant binding info
 * @param tenantKey
 * @returns
 */
export function feishuTenantBindDetail(tenantKey: string) {
  return axios.get(urlcat(Url.SOCIAL_FEISHU_TENANT_BIND_DETAIL, { tenantKey }));
}

/**
 * get lark tenant binding info
 * @param tenantKey
 * @returns
 */
export function getFeiShuTenant(tenantKey: string) {
  return axios.get(urlcat(Url.SOCIAL_FEISHU_TENANT, { tenantKey }));
}

/**
 * change lark tenant admin
 * @param tenantKey
 * @param spaceId
 * @param memberId
 * @returns
 */
export function feishuChangeMainAdmin(tenantKey: string, spaceId: string, memberId: string) {
  return axios.post(Url.SOCIAL_CHANGE_ADMIN, { memberId, spaceId, tenantKey });
}

/*---------- Tencent Cloud IDASS ----------*/

/**
 * Get tencent iDaaS callback URL
 * @param clientId
 * @returns
 */
export function getIDassLoginUrl(clientId: string) {
  return axios.get(`${Url.GET_IDASS_LOGIN_URL}/${clientId}`);
}

/**
 * Tencent iDaaS login callback
 *
 * @param clientId
 * @param code
 * @param state
 * @returns
 */
export function idaasLoginCallback(clientId: string, code: string, state: string) {
  return axios.post(`${Url.IDAAS_LOGIN_CALLBACK}/${clientId}`, { code, state });
}

/**
 * Tencent Cloud iDaaS Contact Sync
 * @returns
 */
export function idaasContactSync() {
  return axios.post(Url.IDAAS_CONTACT_SYNC);
}

/**
 * get info about the Tencent Cloud iDaaS
 *
 * @param spaceId
 * @returns
 */
export function spaceBindIdaasInfo(spaceId: string) {
  return axios.get(urlcat(Url.IDAAS_GET_SPACE_BIND_INFO, { spaceId }));
}

/*---------- Wecom marketplace ----------*/

/**
 * Wecom marketplace, click the app and auto login
 *
 * @param code
 * @param suiteId
 * @returns
 */
export function postWecomAutoLogin(code: string, suiteId: string) {
  return axios.post(Url.POST_WECOM_AUTO_LOGIN, { code, suiteId });
}

/**
 * Wecom, scan the QR code and login
 * @param authCode
 * @param suiteId
 * @returns
 */
export function postWecomScanLogin(authCode: string, suiteId: string) {
  return axios.post(Url.POST_WECOM_SCAN_LOGIN, { authCode, suiteId });
}

/**
 * Retrieve user information when logging in to the space
 * @param authCorpId
 * @param suiteId
 * @returns
 */
export function getWecomLoginInfo(authCorpId: string, suiteId: string) {
  return axios.get(urlcat(Url.GET_WECOM_SPACE_INFO, { authCorpId, suiteId }));
}

/**
 * wecom, admin jump to third-party management page to auto login
 * @param authCode
 * @param suiteId
 * @returns
 */
export function postWecomLoginAdmin(authCode: string, suiteId: string) {
  return axios.post(Url.POST_WECOM_LOGIN_ADMIN, { authCode, suiteId });
}

/**
 * get space info bind with wecom app
 * @param authCorpId
 * @param suiteId
 * @param cpUserId
 * @returns
 */
export function getWecomBindSpacesInfo(authCorpId: string, suiteId: string, cpUserId: string) {
  return axios.get(urlcat(Url.GET_WECOM_TENANT_INFO, { authCorpId, suiteId, cpUserId }));
}

/**
 * wecom change admin
 * @param authCorpId
 * @param memberId
 * @param spaceId
 * @param suiteId
 * @param cpUserId
 * @returns
 */
export function postWecomChangeAdmin(authCorpId: string, memberId: number, spaceId: string, suiteId: string, cpUserId: string) {
  return axios.post(Url.POST_WECOM_CHANGE_ADMIN, { authCorpId, memberId, spaceId, suiteId, cpUserId });
}

/**
 * wecom invite unauth members
 * @param spaceId
 * @param selectedTickets
 * @returns
 */
export function postWecomUnauthMemberInvite(spaceId: string, selectedTickets: string[]) {
  return axios.post(Url.POST_WECOM_UNAUTHMEMBER_INVITE, { spaceId, selectedTickets });
}

/**
 * get wecom common config arguments
 * @param spaceId
 * @param url
 * @returns
 */
export function getWecomCommonConfig(spaceId: string, url: string) {
  return axios.get(Url.GET_WECOM_CONFIG, {
    params: { spaceId, url },
  });
}

/**
 * wecom validation config
 * @param param0
 * @returns
 */
export function socialWecomCheckConfig({
  agentId,
  agentSecret,
  appHomepageUrl,
  authCallbackDomain,
  corpId,
  trustedDomain,
}: {
  agentId: string;
  agentSecret: string;
  appHomepageUrl: string;
  authCallbackDomain: string;
  corpId: string;
  trustedDomain: string;
}) {
  return axios.post(Url.SOCIAL_WECOM_CHECK_CONFIG, { agentId, agentSecret, appHomepageUrl, authCallbackDomain, corpId, trustedDomain });
}

/**
 * Wecom binding config
 * @param configSha
 * @param code
 * @param spaceId
 * @returns
 */
export function socialWecomBindConfig(configSha: string, code: string, spaceId: string) {
  return axios.post(urlcat(Url.SOCIAL_WECOM_BIND_CONFIG, { configSha }), { code, spaceId });
}

/**
 * Wecom domain transfer to IP and validation
 * @param domain
 * @returns
 */
export function socialWecomDomainCheck(domain: string) {
  return axios.post(Url.SOCIAL_WECOM_DOMAIN_CHECK, { domain });
}

/**
 * get wecom config
 * @returns
 */
export function socialWecomGetConfig() {
  return axios.get<IApiWrapper & ISocialWecomGetConfigResponse>(Url.SOCIAL_WECOM_GET_CONFIG);
}

/**
 * get wecom app's binding space id
 * @param corpId
 * @param agentId
 * @returns
 */
export function wecomAgentBindSpace(corpId: string, agentId: string) {
  return axios.get<IApiWrapper & IWecomAgentBindSpaceResponse>(urlcat(Url.WECOM_AGENT_BINDSPACE, { corpId, agentId }));
}

/**
 *
 * get wecom's agentConfig argument
 *
 * @param spaceId
 * @param url
 * @returns
 */
export function getWecomAgentConfig(spaceId: string, url: string) {
  return axios.get(Url.GET_WECOM_AGENT_CONFIG, {
    params: { spaceId, url },
  });
}

// ********************************* DingTalk
/**
 *
 * @param suiteId
 * @param corpId
 * @param code
 * @param bizAppId
 * @returns
 */
export function dingTalkUserLogin(suiteId: string, corpId: string, code: string, bizAppId?: string) {
  const data: Record<string, string> = {
    corpId,
    code,
  };

  if (bizAppId) {
    data.bizAppId = bizAppId;
  }

  return axios.post(urlcat(Url.SOCIAL_DINGTALK_USER_LOGIN, { suiteId }), data);
}

/**
 * DingTalk Bind Space
 * @param suiteId
 * @param corpId
 * @returns
 */
export function dingTalkBindSpace(suiteId: string, corpId: string) {
  return axios.get(urlcat(Url.SOCIAL_DINGTALK_BIND_SPACE, { suiteId }), {
    params: {
      corpId,
    },
  });
}

/**
 * DingTalk - get tenant bind info
 * @param suiteId
 * @param corpId
 * @returns
 */
export function dingTalkAdminDetail(suiteId: string, corpId: string) {
  return axios.get(urlcat(Url.SOCIAL_DINGTALK_ADMIN_DETAIL, { suiteId }), {
    params: {
      corpId,
    },
  });
}

/**
 * DingTalk - admin login
 * @param suiteId
 * @param code
 * @param corpId
 * @returns
 */
export function dingTalkAdminLogin(suiteId: string, code: string, corpId: string) {
  const data: Record<string, string> = { code };

  if (corpId) {
    data.corpId = corpId;
  }

  return axios.post(urlcat(Url.SOCIAL_DINGTALK_ADMIN_LOGIN, { suiteId }), data);
}

/**
 * DingTalk - change admin
 * @param suiteId
 * @param corpId
 * @param spaceId
 * @param memberId
 * @returns
 */
export function dingTalkChangeAdmin(suiteId: string, corpId: string, spaceId: string, memberId: string) {
  return axios.post(urlcat(Url.SOCIAL_DINGTALK_CHANGE_ADMIN, { suiteId }), { corpId, spaceId, memberId });
}

/**
 * DingTalk H5 User Login callback
 * @param agentId
 * @param code
 * @returns
 */
export function dingtalkH5UserLogin(agentId: string, code: string) {
  return axios.post(urlcat(Url.DINGTALK_H5_USER_LOGIN, { agentId }), { code });
}

/**
 * DingTalk bind space
 * @param agentId
 * @param spaceId
 * @returns
 */
export function dingtalkH5BindSpace(agentId: string, spaceId: string) {
  return axios.post(urlcat(Url.DINGTALK_H5_BIND_SPACE, { agentId }), { spaceId });
}

/**
 * DingTalk H5 bind space
 *
 * @param agentId
 * @returns
 */
export function getDingtalkH5BindSpaceId(agentId: string) {
  return axios.get(urlcat(Url.DINGTALK_H5_BIND_SPACE, { agentId }));
}

/**
 * DingTalk - refresh organization
 *
 * @returns
 */
export function freshDingtalkOrg() {
  return axios.get(Url.DINGTALK_REFRESH_ORG);
}

export function freshWecomOrg() {
  return axios.get(Url.WECOM_REFRESH_ORG);
}

export function getDingtalkSKU(spaceId: string, callbackPage: string) {
  return axios.post(Url.SOCIAL_DINGTALK_SKU, { spaceId, callbackPage });
}

export function getDingtalkConfig(spaceId: string, url: string) {
  return axios.post(Url.SOCIAL_DINGTALK_CONFIG, { spaceId, url });
}

export function dingtalkLoginCallback(state: string, code: string, type = 0) {
  return axios.get(Url.DINGTALK_LOGIN_CALLBACK, {
    params: {
      state,
      code,
      type,
    },
  });
}

export function qqLoginCallback(code: string, accessToken: string, expiresIn: string, type = 0) {
  return axios.get(Url.QQ_LOGIN_CALLBACK, {
    params: {
      code,
      accessToken,
      expiresIn,
      type,
    },
  });
}

export function wechatLoginCallback(code: string, state: string) {
  return axios.get(Url.WECHAT_LOGIN_CALLBACK, {
    params: {
      code,
      state,
    },
  });
}

export function wecomLoginCallback(code: string, agentId: string, corpId: string) {
  return axios.post(Url.WECOM_LOGIN_CALLBACK, { code, agentId, corpId });
}

export function woaLoginCallback(code: string, appId: string) {
  return axios.post(Url.WOA_CALLBACK, { code, appId });
}

export function woaRefreshContact() {
  return axios.post(Url.WOA_REFRESH_CONTACT);
}

/**
 * wechat miniapp poll check
 * @param {(0 | 1 | 2)} type
 * 0：web qr code login
 * 1：web account binding
 * 2：miniapp is waiting get into workbench
 * @param {string} mark  QR Code ID
 * @returns
 */
export function poll(type: QrAction, mark: string) {
  return axios.get(Url.POLL, {
    params: {
      type,
      mark,
    },
  });
}

/**
 * @description Get QR Code
 * @param {number} type
 * 0：login
 * 1：binding
 * @returns
 */
export function getQrCode(type: number, page?: string, width?: number) {
  return axios.get(Url.WECHAT_QR_CODE, {
    params: {
      type,
      page,
      width,
    },
  });
}

/**
 * get wechat share signature
 * @param url
 * @returns
 */
export function getWechatSignature(url: string) {
  return axios.post(Url.WECHAT_MP_SIGNATURE, {
    url,
  });
}

/**
 * Generate or Refresh wechat public account QR code
 *
 * @param type
 * @returns
 */
export function getOfficialAccountsQrCode(type: number) {
  return axios.get(Url.OFFICIAL_ACCOUNTS_QRCODE, {
    params: {
      type,
    },
  });
}

/**
 * Poll wechat public account (media platform)
 *
 * @param mark
 * @param type
 * @returns
 */
export function officialAccountsPoll(mark: string, type: number) {
  return axios.get(Url.OFFICIAL_ACCOUNTS_POLL, {
    params: {
      mark,
      type,
    },
  });
}

/**
 * get the social tenant env config
 * @returns
 */
export function socialTenantEnv() {
  return axios.get(Url.SOCIAL_TENANT_ENV);
}

export function syncOrgMembers(data: ISyncMemberRequest) {
  return axios.post<IApiWrapper>(Url.SYNC_ORG_MEMBERS, data);
}

// eslint-disable-next-line max-len
export function queryOrderPriceList(product: 'GOLD' | 'SILVER') {
  return axios.get<IApiWrapper & { data: IQueryOrderPriceResponse[] }>(UrlBilling.ORDER_PRICE, {
    params: {
      product,
    },
  });
}

export function createOrder(params: { month: number; product: string; seat: number; spaceId: string }) {
  return axios.post<IApiWrapper & { data: ICreateOrderResponse }>(UrlBilling.ORDER_CREATE, params);
}

export function payOrder(orderId: string, payChannel: 'new_wx_pub_qr' | 'new_alipay_pc_direct') {
  return axios.post<IApiWrapper & { data: IPayOrderResponse }>(urlcat(UrlBilling.ORDER_PAYMENT, { orderId }), { payChannel });
}

export function queryOrderStatus(orderId: string) {
  return axios.get<IApiWrapper & { data: IQueryOrderStatusResponse }>(urlcat(UrlBilling.ORDER_STATUS, { orderId }));
}

export function queryOrderDiscount(params: any) {
  return axios.post<IApiWrapper & { data: IQueryOrderDiscountResponse }>(UrlBilling.DRY_RUN, params);
}

export function paidCheck(orderId: string) {
  return axios.get<IApiWrapper & { data: IQueryOrderStatusResponse }>(urlcat(UrlBilling.PAID_CHECK, { orderId }));
}

/* APITable Cloud */

export function apitableChangePasswordEmail() {
  return axios.post<IApiWrapper>(Url.APITABLE_CHANGE_PASSWORD);
}

export function apitableResentVerifyEmail(email: string) {
  return axios.post<IApiWrapper>(Url.APITABLE_RESENT_VERIFY_EMAIL, {
    email,
  });
}

export function getSubscript(spaceId: string | null) {
  return axios.get(Url.BILLING_SUBSCRIPTION, {
    params: {
      spaceId,
    },
  });
}

export function updateBillingSubscription(spaceId: string | null, subscriptionId: string, action?: string) {
  return axios.post(urlcat(Url.UPDATE_SUBSCRIPTION, { spaceId, subscriptionId, action }));
}

export function changePaymentMethod(spaceId: string | null) {
  return axios.post(urlcat(Url.CHANGE_PAYMENT_METHOD, { spaceId }));
}

export function cancelSubscription(spaceId: string | null, subscriptionId: string) {
  return axios.post(urlcat(Url.CANCEL_SUBSCRIPTION, { spaceId, subscriptionId }));
}

export function getInvoices(spaceId: string | null, startingAfter?: string, endingBefore?: string, limit?: number) {
  return axios.get(Url.GET_INVOICES, {
    params: {
      spaceId,
      startingAfter,
      endingBefore,
      limit,
    },
  });
}

export function changePlanUserInfo(spaceId: string | null) {
  return axios.post(urlcat(Url.CHANGE_PLAN_USER_INFO, { spaceId }));
}

// get workdoc info
export const getWorkdocInfo = (documentId: string) => {
  return axios.get(urlcat(Url.GET_WORKDOC_INFO, { documentId }), {
    baseURL: '/api/v1/',
  });
};
