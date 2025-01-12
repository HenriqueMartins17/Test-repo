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

import { useMount } from 'ahooks';
import * as dd from 'dingtalk-jsapi';
import { IRuntimePermissionRequestAuthCodeParams } from 'dingtalk-jsapi/api/runtime/permission/requestAuthCode';
import * as React from 'react';
import { useRef, useState } from 'react';
import { Api, ConfigConstant, Navigation, StatusCode, Strings, t } from '@apitable/core';
import { Loading, Message, Modal } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest } from 'pc/hooks';
import { BindSpace, IBindSpaceRef, formatSelectOptionData, IFormatSelectOptionData, isInDingtalkFunc } from '../../home';

const DevCode = 'de5eaed5dd1936f382953842c241945b';

const getDefaultValueRule = (info: IFormatSelectOptionData, memberCount: number) =>
  info.social && !info.social.enabled && info.maxSeat >= memberCount;

export const DingTalkH5Login = () => {
  const query = useQuery();
  const bindSpaceRef = useRef<IBindSpaceRef>();
  const agentId = query.get('agentId') || '';
  const corpId = query.get('corpId') || '';
  const reference = query.get('reference') || '';
  const [optionData, setOptionData] = useState<IFormatSelectOptionData[]>();
  const [memberCount, setMemberCount] = useState(0);
  const [curSpaceName, setCurSpaceName] = useState('');
  const { run: userLogin } = useRequest(code => Api.dingtalkH5UserLogin(agentId, code), {
    manual: true,
    onSuccess: res => {
      const { data, success, message, code } = res.data;
      if (!success) {
        if (code === StatusCode.DINGTALK_NOT_BIND_SPACE) {
          Router.push(Navigation.DINGTALK, {
            params: { dingtalkPath: 'unbound_err' },
          });
          return;
        }
        Message.error({ content: message });
        return;
      }
      if (data.bindSpaceId) {
        Router.push(Navigation.WORKBENCH, {
          params: { spaceId: data.bindSpaceId },
          query: { reference },
        });
        return;
      }
      const dataArr = formatSelectOptionData(data.spaces);
      const value = dataArr.find(item => getDefaultValueRule(item, data.activeMemberCount));
      if (value) {
        setCurSpaceName(value.name);
      }
      setOptionData(dataArr);
      setMemberCount(data.activeMemberCount);
    },
    onError: res => {
      Message.error({ content: res.message });
    }
  });

  const { run: bindSpace, loading } = useRequest(spaceId => Api.dingtalkH5BindSpace(agentId, spaceId), {
    manual: true,
    onSuccess: (res, params) => {
      const { success, message } = res.data;
      if (!success) {
        Message.error({ content: message });
        return;
      }
      Router.push(Navigation.WORKBENCH, {
        params: { spaceId: params[0] },
        query: { reference },
      });
      return;
    },
    onError: res => {
      Message.error({ content: res.message });
    }
  });

  useMount(() => {
    if (isInDingtalkFunc()) {
      dd.runtime.permission.requestAuthCode({
        corpId: corpId,
        onSuccess: (res: { code: any; }) => userLogin(res.code),
        onFail: function (err: { errorMessage: any; }) {
          Message.error({ content: err.errorMessage });
        }
      } as IRuntimePermissionRequestAuthCodeParams);
    } else {
      userLogin(DevCode);
    }
  });

  const onBindSpaceClick = React.useCallback((value: React.Key) => {
    Modal.confirm({
      type: 'warning',
      title: t(Strings.extra_tip),
      content: t(Strings.dingtalk_bind_space_tips, { spaceName: curSpaceName }),
      okText: t(Strings.bind),
      cancelText: t(Strings.do_not_bind),
      onOk: () => bindSpace(value),
    });
  }, [bindSpace, curSpaceName]);

  const onBindSpaceChange = React.useCallback((value: React.Key) => {
    const curData = optionData!.find(item => (item.value && item.value === value));
    if (curData!.social && curData!.social.enabled) {
      bindSpaceRef && bindSpaceRef.current && bindSpaceRef.current.setErr(t(Strings.social_plat_bind_space_bound_err));
    } else {
      curData && setCurSpaceName(curData.name);
      bindSpaceRef && bindSpaceRef.current && bindSpaceRef.current.setErr('');
    }
  }, [bindSpaceRef, optionData]);

  const defaultValue = React.useMemo(() => {
    if (!Array.isArray(optionData) || optionData.length < 1) {
      return undefined;
    }
    const value = optionData.find(item => getDefaultValueRule(item, memberCount));
    return value ? value.spaceId : undefined;
  }, [optionData, memberCount]);

  if (memberCount === 0 || !optionData) {
    return <Loading />;
  }
  return (
    <BindSpace
      type={ConfigConstant.SocialType.DINGTALK}
      optionData={optionData}
      maxCount={memberCount}
      onClick={onBindSpaceClick}
      onChange={onBindSpaceChange}
      btnLoading={loading}
      defaultValue={defaultValue}
      ref={bindSpaceRef}
    />
  );
};
