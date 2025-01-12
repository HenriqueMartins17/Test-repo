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

import { Modal } from 'antd';
import classnames from 'classnames';
import Image from 'next/image';
import * as React from 'react';
import { createRoot } from 'react-dom/client';
import { Button, colorVars, stopPropagation, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { CloseOutlined, StarFilled } from '@apitable/icons';
// import { expandUpgradeSpace } from 'pc/components/space_manage/upgrade_space/expand_upgrade_space';
import { showStripePricingTable } from 'components/billing/show_stripe_pricing_table';
import { getEnvVariables } from 'pc/utils/env';
// import { getNodeIcon } from 'pc/components/catalog/tree/node_icon';
// import { goToUpgrade } from '../upgrade_method';
import styles from './styles.module.scss';

interface IUsageWarnModalParams {
  alertContent: string;
  email: string;
  reload?: boolean;
}

const Avatar: React.FC<{ opacity: number }> = ({ opacity }) => {
  const colors = useThemeColors();
  return (
    <div className={styles.avatar} style={{ borderColor: `rgba(123, 103, 238, ${opacity})` }}>
      <div className={styles.avatarIcon}>
        <Image src={getEnvVariables().ONBOARDING_CUSTOMER_SERVICE_QRCODE_AVATAR_IMG!} alt="" width={64} height={64} />
      </div>
      <div className={styles.avatarStar}>
        <StarFilled color={colors.fc14} size="12px" />
      </div>
    </div>
  );
};

const UsageWarnModalInner: React.FC<IUsageWarnModalParams> = ({ alertContent, email }) => {
  const _goToUpgrade = () => {
    showStripePricingTable(email);
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.main}>
        <Avatar opacity={0.3} />
        <Typography variant="h5" className={classnames(styles.textCenter, styles.alertTitle)}>
          {t(Strings.usage_overlimit_alert_title)}
        </Typography>
        <Typography variant="body1" className={classnames(styles.alertContent)}>
          {alertContent}
        </Typography>
        <div className={styles.buttonGroup}>
          <Button color="primary" size={'middle'} className={styles.upgradeBtn} onClick={_goToUpgrade} block>
            <span style={{ position: 'relative' }}>
              {/*{getNodeIcon('star2', ConfigConstant.NodeType.DATASHEET, {*/}
              {/*  size: ConfigConstant.CELL_EMOJI_SIZE,*/}
              {/*  emojiSize: ConfigConstant.CELL_EMOJI_SIZE,*/}
              {/*})}*/}
            </span>
            <span style={{ position: 'relative', left: 3 }}>{t(Strings.upgrade_now)}</span>
          </Button>
        </div>
      </div>
    </div>
  );
};

export const usageWarnModal = (params: IUsageWarnModalParams) => {
  const container = document.createElement('div');
  document.body.appendChild(container);
  const root = createRoot(container);
  const onModalClose = () => {
    root.unmount();
    container.parentElement!.removeChild(container);
  };

  root.render(
    <div onMouseDown={stopPropagation}>
      <Modal
        visible
        wrapClassName={styles.modalWrapper}
        maskClosable={false}
        closeIcon={<CloseOutlined color={colorVars.fc3} size={8} />}
        onCancel={onModalClose}
        destroyOnClose
        width={400}
        // width={getEnvVariables().HIDDEN_BUSINESS_SUPPORT_PROGRAM_MODAL ? '360px' : '720px'}
        footer={null}
        centered
        zIndex={1100}
      >
        <UsageWarnModalInner {...params} />
      </Modal>
    </div>,
  );
};
