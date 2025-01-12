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

import * as React from 'react';
import { useContext, useState } from 'react';
import { Button, Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { MarketingContext } from '../context';
import { AppStatus, IStoreApp } from '../interface';
import style from '../style.module.less';
import { AppModal } from './app_modal';

interface ICard extends IStoreApp {
  openStatus: AppStatus;
}

export const Card: React.FC<ICard> = props => {
  const { openStatus, logoUrl, name, intro } = props;
  const { onSetRefresh } = useContext(MarketingContext);
  const [modalVisible, setModalVisible] = useState(false);

  const isOpen = openStatus === AppStatus.Open;
  const btnText = isOpen ? 'social_open_card_btn_text'
    : 'marketplace_integration_btncard_appsbtntext_read_more';

  const handleRefresh = () => {
    onSetRefresh((val) => !val);
  };

  return (
    <>
      <div className={style.card}>
        <div className={style.top}>
          <div className={style.icon}>
            <img src={logoUrl} width={46} height={46} alt={''} />
          </div>
          <Typography variant='h6'>
            {name}
          </Typography>
        </div>

        <div className={style.middle}>
          <p>{intro}</p>
        </div>

        <div className={style.bottom}>
          <Button
            style={{ fontSize: 12 }}
            color='primary'
            variant={isOpen ? 'jelly' : undefined}
            block
            onClick={() => {
              setModalVisible(true);
            }}
            size='small'
          >
            <span>{t(Strings[btnText])}</span>
          </Button>
        </div>
      </div>

      {modalVisible && <AppModal onRefresh={handleRefresh} onClose={() => setModalVisible(false)} {...props} />}
    </>
  );
};
