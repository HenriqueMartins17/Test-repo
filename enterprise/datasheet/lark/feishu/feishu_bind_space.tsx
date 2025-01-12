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
import classNames from 'classnames';
import parser from 'html-react-parser';
import Image from 'next/image';
import { useEffect, useState } from 'react';
import { Button, Skeleton } from '@apitable/components';
import { Api, ISpaceInfo, Navigation, Settings, StatusCode, Strings, t } from '@apitable/core';
import { Loading, LoginCard, Message, MobileSelect, Modal, Wrapper } from 'pc/components/common';
import { ScreenSize } from 'pc/components/common/component_display';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useRequest, useResponsive } from 'pc/hooks';
import BothImg from 'static/icon/signin/signin_img_vika_feishu.png';
import { CustomSelect } from './custom_select';
import styles from './style.module.less';

interface IEnhancedSpaceInfo extends ISpaceInfo {
  label: string;
  value: string;
}

const FeiShuBindSpace = () => {
  const { screenIsAtMost } = useResponsive();
  const query = useQuery();
  // const openId = query.get('openId');
  const tenantKey = query.get('tenantKey') || query.get('tenant_key');
  const isMobile = screenIsAtMost(ScreenSize.md);
  const [curSpace, setCurSpace] = useState<IEnhancedSpaceInfo | null>(null);
  const [optionData, setOptionData] = useState<IEnhancedSpaceInfo[]>([]);
  const [maxCount, setMaxCount] = useState<number>();
  const [err, setErr] = useState<React.ReactNode>('');

  // Get a list of manageable spaces
  const { loading: listLoading } = useRequest(() => Api.spaceList(true), {
    onSuccess: res => {
      const { data, success, message } = res.data;
      if (success) {
        const option: IEnhancedSpaceInfo[] = [];
        data.forEach((item: IEnhancedSpaceInfo) => {
          if (item.preDeleted || (item.social && item.social.enabled)) {
            return;
          }
          option.push({
            ...item,
            value: item.spaceId,
            label: item.name + `（${item.maxSeat}人）`,
          });
        });
        if (option.length < 1) {
          return;
        }
        setOptionData(option);
      } else {
        Message.error({ content: message });
      }
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
  });
  const { run: getFeiShuTenantInfo, loading: numberLoading } = useRequest(tenantKey => Api.getFeiShuTenantInfo(tenantKey), {
    onSuccess: res => {
      const { data, success, message } = res.data;
      if (success) {
        setMaxCount(data.memberCount);
      } else {
        Message.error({ content: message });
      }
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true,
  });

  const { loading: btnLoading, run: bindSpace } = useRequest((tenantKey, spaceList) => Api.socialFeiShuBindSpace(tenantKey, spaceList), {
    onSuccess: res => {
      const { success, message, code } = res.data;
      if (success) {
        setErr('');
        Router.push(Navigation.WORKBENCH, {
          params: { spaceId: curSpace!.spaceId },
        });
      } else if (code === StatusCode.PAYMENT_PLAN) {
        Modal.confirm({
          type: 'warning',
          title: t(Strings.please_note),
          okText: t(Strings.submit_requirements),
          onOk: () => {
            window.open(Settings.integration_feishu_seats_form_url.value);
          },
          content: t(Strings.feishu_bind_space_need_upgrade, {
            maxSeat: String(curSpace?.maxSeat),
            maxCount,
          }),
        });
        return;
      } else {
        setErr(message);
        return;
      }
    },
    onError: () => {
      Message.error({ content: t(Strings.error) });
    },
    manual: true,
  });
  useMount(() => {
    tenantKey && getFeiShuTenantInfo(tenantKey);
  });
  useEffect(() => {
    if (numberLoading || listLoading || typeof maxCount !== 'number') {
      return;
    }
    const defaultSelect = optionData.find(item => numberRule(item.maxSeat, maxCount));
    defaultSelect && setCurSpace(defaultSelect);
  }, [numberLoading, listLoading, maxCount, optionData]);
  const numberRule = (number: number, condition?: number) => {
    return condition && number >= condition;
  };
  const onChange = (value: string) => {
    const space = optionData.find(item => item.spaceId === value);
    if (space) {
      setCurSpace(space);
      const errStr = numberRule(space.maxSeat, maxCount) ? '' : parser(t(Strings.feishu_bind_space_err, { count: space.maxSeat }));
      setErr(errStr);
    }
  };
  const bindSpaceBtnClick = () => {
    if (err || !curSpace) {
      return;
    }
    Modal.confirm({
      type: 'warning',
      title: t(Strings.extra_tip),
      content: t(Strings.feishu_bind_space_tips, { spaceName: curSpace.name }),
      okText: t(Strings.bind),
      cancelText: t(Strings.do_not_bind),
      onOk: () => bindSpace(tenantKey, [{ spaceId: curSpace.spaceId }]),
    });
  };
  if (!tenantKey) {
    return <Loading />;
  }
  return (
    <Wrapper hiddenLogo className={styles.center}>
      <div className={classNames(styles.commonWrapper, styles.bindSpaceWrapper, styles.center)}>
        <div className={styles.commonImgWrapper}>
          <Image src={BothImg} alt="" />
        </div>
        <LoginCard className={classNames(styles.commonLoginCardWrapper, styles.bindSpaceCard)}>
          <div className={styles.cardTop}>
            <div className={styles.commonCardTitle}>{t(Strings.feishu_bind_space_select_title)}</div>
            {listLoading || numberLoading ? (
              <div style={{ width: '100%' }}>
                <Skeleton count={2} />
                <Skeleton width='61%' />
              </div>
            ) : isMobile ? (
              <MobileSelect
                value={curSpace?.spaceId}
                defaultValue={curSpace?.spaceId}
                onChange={onChange}
                optionData={optionData}
                title={t(Strings.feishu_bind_space_select_title)}
                style={{ height: '40px' }}
              />
            ) : (
              <CustomSelect
                value={curSpace ? curSpace.spaceId : undefined}
                onChange={onChange}
                optionData={optionData}
                defaultOpen
                autoFocus
                listHeight={256}
              />
            )}
            {err && <div className={styles.err}>{err}</div>}
            <div className={styles.subTitle}>{t(Strings.feishu_bind_space_config_title)}</div>
            {numberLoading ? (
              <div style={{ width: '100%' }}>
                <Skeleton count={2} />
                <Skeleton width='61%' />
              </div>
            ) : (
              <div className={styles.desc}>{t(Strings.feishu_bind_space_config_detail, { maxCount })}</div>
            )}
          </div>
          <Button color='primary' block size='large' onClick={bindSpaceBtnClick} loading={btnLoading} disabled={btnLoading}>
            {t(Strings.feishu_bind_space_btn)}
          </Button>
        </LoginCard>
      </div>
    </Wrapper>
  );
};

export default FeiShuBindSpace;
