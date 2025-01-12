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

import classnames from 'classnames';
import Image from 'next/image';
import React from 'react';
import { createRoot } from 'react-dom/client';
import { Button, colorVars, TextButton, Typography, useThemeColors } from '@apitable/components';
import { ConfigConstant, integrateCdnHost, Strings, t } from '@apitable/core';
import { CloseOutlined, StarFilled } from '@apitable/icons';
import { getNodeIcon } from 'pc/components/catalog/tree/node_icon';
import { Modal } from 'pc/components/common/modal/modal/modal';
import { stopPropagation } from 'pc/utils/dom';
import { getEnvVariables } from 'pc/utils/env';
import { goToUpgrade } from '../upgrade_method';
import { isSaaSApp } from '../usage_warn_modal/utils';
import styles from './styles.module.less';

interface IUsageWarnModalParams {
  alertContent: string;
  reload?: boolean;
  title?: string;
}

const UsageWarnModalInner: React.FC<IUsageWarnModalParams> = ({ alertContent, reload, title }) => {
  const colors = useThemeColors();

  const renderAvatar = (opacity: number) => (
    <div className={styles.avatar} style={{ borderColor: `rgba(123, 103, 238, ${opacity})` }}>
      <div className={styles.avatarIcon}>
        <Image src={getEnvVariables().ONBOARDING_CUSTOMER_SERVICE_QRCODE_AVATAR_IMG!} alt="" width={64} height={64} />
      </div>
      <div className={styles.avatarStar}>
        <StarFilled color={colors.fc14} size="12px" />
      </div>
    </div>
  );

  const renderQrCode = (size: number) => (
    <div className={styles.qrCodeImage} style={{ width: size, height: size }}>
      <div className={styles.qrCodeImageCorner} />
      <div className={styles.qrCodeImageCorner} />
      <div className={styles.qrCodeImageCorner} />
      <div className={styles.qrCodeImageCorner} />
      <Image width={size - 20} height={size - 20} src={integrateCdnHost(getEnvVariables().BILLING_PAYMENT_PAGE_CONTACT_US_IMG!)} />
    </div>
  );
  const _goToUpgrade = () => {
    goToUpgrade();
    reload && location.reload();
  };

  const _viewDetail = () => {
    window.open('/pricing', '_blank', 'noopener,noreferrer');
    reload && location.reload();
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.pageLeft} style={{ width: getEnvVariables().HIDDEN_BUSINESS_SUPPORT_PROGRAM_MODAL ? '100%' : '50%' }}>
        {renderAvatar(0.3)}
        <Typography variant="h5" className={classnames(styles.textCenter, styles.alertTitle)}>
          {title || t(Strings.usage_overlimit_alert_title)}
        </Typography>
        <Typography variant="body1" className={classnames(styles.alertContent)}>
          {alertContent}
        </Typography>
        <div className={styles.buttonGroup}>
          <Button color="primary" size={'middle'} className={styles.upgradeBtn} onClick={_goToUpgrade} block>
            <span style={{ position: 'relative' }}>
              {getNodeIcon('star2', ConfigConstant.NodeType.DATASHEET, {
                size: ConfigConstant.CELL_EMOJI_SIZE,
                emojiSize: ConfigConstant.CELL_EMOJI_SIZE,
              })}
            </span>
            <span style={{ position: 'relative', left: 3 }}>{t(Strings.upgrade_now)}</span>
          </Button>
          {isSaaSApp() && !getEnvVariables().IS_APITABLE && (
            <TextButton color="default" className={styles.checkMorePrivileges} onClick={_viewDetail} block>
              {t(Strings.check_more_privileges)}
            </TextButton>
          )}
        </div>
      </div>
      {!getEnvVariables().HIDDEN_BUSINESS_SUPPORT_PROGRAM_MODAL && (
        <div className={styles.pageRight}>
          <div className={styles.qrCodeImageContentBorder}>
            <Typography variant="h6" className={styles.qrCodeImageTip}>
              {t(Strings.startup_company_support_program)}
            </Typography>
            <Typography variant="body3" className={styles.qrCodeImageSubTip}>
              {t(Strings.contact_us_to_join_company_support)}
            </Typography>
            {renderQrCode(194)}
          </div>
        </div>
      )}
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
    params.reload && location.reload();
  };

  root.render(
    <div onMouseDown={stopPropagation}>
      <Modal
        visible
        wrapClassName={classnames(styles.modalWrapper, {
          [styles.vikaUsageWarnModal]: !getEnvVariables().IS_APITABLE,
        })}
        maskClosable={false}
        closeIcon={<CloseOutlined color={colorVars.textCommonTertiary} size={8} />}
        onCancel={onModalClose}
        destroyOnClose
        width={getEnvVariables().HIDDEN_BUSINESS_SUPPORT_PROGRAM_MODAL ? '360px' : '720px'}
        footer={null}
        centered
        zIndex={1100}
      >
        <UsageWarnModalInner {...params} />
      </Modal>
    </div>
  );
};
