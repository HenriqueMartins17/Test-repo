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

import classNames from 'classnames';
import Image from 'next/image';
import * as React from 'react';
import { forwardRef, useImperativeHandle, useState } from 'react';
import { Button, Skeleton } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { Loading, LoginCard, MobileSelect, Wrapper } from 'pc/components/common';
import { ScreenSize } from 'pc/components/common/component_display';
import { useResponsive } from 'pc/hooks';
//@ts-ignore
import { SocialPlatformMap } from '../../config';
import { IFormatSelectOptionData } from '../../utils';
import { CustomSelect } from 'enterprise/lark/feishu/custom_select';
import styles from './style.module.less';

interface IBindSpaceBase {
  loading?: boolean;
  btnLoading?: boolean;
  optionData?: IFormatSelectOptionData[] | null;
  err?: React.ReactNode;
  defaultValue?: React.Key;
  onChange?: (value: React.Key) => void;
  onClick?: (value: React.Key) => void;
  maxCount?: number | null;
  type: 1 | 2 | 3;
}
export interface IBindSpaceRef {
  setErr: (err?: React.ReactNode) => void;
}
const BindSpaceBase = (props: IBindSpaceBase, ref: React.Ref<unknown>) => {
  useImperativeHandle(
    ref,
    () =>
      ({
        setErr: (err?: React.ReactNode) => {
          setSelectErr(err);
        },
      } as IBindSpaceRef),
  );
  const { screenIsAtMost } = useResponsive();
  const { loading, btnLoading, optionData, err, onClick, maxCount, onChange, type, defaultValue } = props;
  const isMobile = screenIsAtMost(ScreenSize.md);
  const [selectErr, setSelectErr] = useState(err);
  const [value, setValue] = useState<React.Key | undefined>(defaultValue);
  const handleChange = (value: any) => {
    setValue(value);
    onChange && onChange(value);
  };

  const handleClick = () => {
    if (err || selectErr || !value) return;
    onClick && onClick(value);
  };
  if (loading) return <Loading />;
  const {
    logoWithVika,
    bindSpace: { cardTitle, subTitle, desc },
  } = SocialPlatformMap[type];
  return (
    <Wrapper hiddenLogo className={'center'}>
      <div className={classNames('commonWrapper', styles.bindSpaceWrapper)}>
        <div className={'commonImgWrapper'}>
          <Image src={logoWithVika as string} alt="" />
        </div>
        <LoginCard className={classNames('commonLoginCardWrapper', styles.bindSpaceCard)}>
          <div className={styles.cardTop}>
            <div className={'commonCardTitle'}>{t(Strings.feishu_bind_space_select_title)}</div>
            {!Array.isArray(optionData) || typeof maxCount !== 'number' ? (
              <div style={{ width: '100%' }}>
                <Skeleton count={2} />
                <Skeleton width="61%" />
              </div>
            ) : isMobile ? (
              <MobileSelect
                value={value}
                defaultValue={value}
                onChange={handleChange}
                optionData={optionData || []}
                title={cardTitle}
                className={styles.mobileSelectWrap}
              />
            ) : (
              CustomSelect && <CustomSelect
                value={value}
                onChange={handleChange}
                optionData={optionData || []}
                defaultOpen
                autoFocus
                listHeight={256}
                dropdownClassName={styles.bindDropdown}
              />
            )}
            {<div className={styles.err}>{err || selectErr}</div>}
            <div className={styles.subTitle}>{subTitle}</div>
            {typeof maxCount !== 'number' ? (
              <div style={{ width: '100%' }}>
                <Skeleton count={2} />
                <Skeleton width="61%" />
              </div>
            ) : (
              <div className={styles.desc}>{desc(maxCount)}</div>
            )}
          </div>
          <Button
            color="primary"
            block
            size="large"
            onClick={handleClick}
            loading={btnLoading}
            disabled={Boolean(btnLoading || err || selectErr || !value)}
          >
            {t(Strings.feishu_bind_space_btn)}
          </Button>
        </LoginCard>
      </div>
    </Wrapper>
  );
};
export const BindSpace = forwardRef(BindSpaceBase);
