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

import parser from 'html-react-parser';
import { StaticImageData } from 'next/image';
import * as React from 'react';
import { ConfigConstant, Strings, t } from '@apitable/core';
import { Modal } from 'pc/components/common/modal/modal/modal';
import DingtalkLogo from 'static/icon/signin/signin_img_dingding.png';
import FeishuLogo from 'static/icon/signin/signin_img_feishu.png';
import DingtalkAndVikaLogo from 'static/icon/signin/signin_img_vika_dingding.png';
import FeishuAndVikaLogo from 'static/icon/signin/signin_img_vika_feishu.png';
import WecomAndVikaLogo from 'static/icon/signin/signin_img_vika_wecom.png';
import WecomLogo from 'static/icon/signin/signin_img_wecom.png';
import WoaLogo from 'static/icon/signin/signin_img_woa.png';

export const SocialPlatformMap: { [key in ConfigConstant.SocialType]: {
  logo: React.ReactNode | StaticImageData,
  name: string,
  logoWithVika: React.ReactNode | StaticImageData,
  bindSpace: {
    cardTitle: string,
    subTitle: React.ReactNode,
    desc: (maxCount: any)=> React.ReactNode,
  },
  toolTipInSpaceListItem: string,
  org_manage_reject_default_msg: string,
  org_manage_reject_default_modal: () => void,
} } = {
  [ConfigConstant.SocialType.WECOM]: {
    logo: WecomLogo,
    name: t(Strings.wecom),
    logoWithVika: WecomAndVikaLogo,
    bindSpace: {
      cardTitle: t(Strings.feishu_bind_space_select_title),
      subTitle: t(Strings.configuration_available_range),
      desc: (maxCount) => `与企业微信可使用范围一致（${maxCount}位成员）`
    },
    toolTipInSpaceListItem: t(Strings.wecom_space_list_item_tag_info),
    org_manage_reject_default_msg: t(Strings.wecom_org_manage_reject_msg),
    org_manage_reject_default_modal: () => {
      Modal.warning({
        title: t(Strings.kindly_reminder),
        content: parser(t(Strings.wecom_org_manage_reject_msg)),
      });
    },
  },
  [ConfigConstant.SocialType.DINGTALK]: {
    logo: DingtalkLogo,
    name: t(Strings.dingtalk),
    logoWithVika: DingtalkAndVikaLogo,
    bindSpace: {
      cardTitle: t(Strings.feishu_bind_space_select_title),
      subTitle: t(Strings.configuration_available_range),
      desc: (maxCount) => t(Strings.dingtalk_bind_space_config_detail, { maxCount })
    },
    toolTipInSpaceListItem: t(Strings.dingtalk_space_list_item_tag_info),
    org_manage_reject_default_msg: t(Strings.dingtalk_change_admin_reject_msg),
    org_manage_reject_default_modal: () => {
      Modal.warning({
        title: t(Strings.kindly_reminder),
        content: parser(t(Strings.dingtalk_org_manage_reject_msg)),
      });
    },
  },
  [ConfigConstant.SocialType.FEISHU]: {
    logo: FeishuLogo,
    name: t(Strings.lark),
    logoWithVika: FeishuAndVikaLogo,
    bindSpace: {
      cardTitle: t(Strings.feishu_bind_space_select_title),
      subTitle: t(Strings.feishu_bind_space_config_title),
      desc: (maxCount) => t(Strings.feishu_bind_space_config_detail, { maxCount })
    },
    toolTipInSpaceListItem: t(Strings.feishu_space_list_item_tag_info),
    org_manage_reject_default_msg: t(Strings.lark_org_manage_reject_msg),
    org_manage_reject_default_modal: () => {
      Modal.warning({
        title: t(Strings.kindly_reminder),
        content: t(Strings.lark_org_manage_reject_msg),
      });
    },
  },
  [ConfigConstant.SocialType.WOA]: {
    logo: WoaLogo,
    name: '金山协作',
    logoWithVika: WoaLogo,
    bindSpace: {
      cardTitle: t(Strings.feishu_bind_space_select_title),
      subTitle: t(Strings.configuration_available_range),
      desc: (maxCount) => `与金山协作可使用范围一致（${maxCount}位成员）`
    },
    toolTipInSpaceListItem: 'Woa binding',
    org_manage_reject_default_msg: t(Strings.wecom_org_manage_reject_msg),
    org_manage_reject_default_modal: () => {
      Modal.warning({
        title: t(Strings.kindly_reminder),
        content: parser(t(Strings.wecom_org_manage_reject_msg)),
      });
    },
  },
};
