/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { Modal, Tooltip } from 'antd';
import { Popconfirm } from 'components/popconfirm';
import { ShareQrCode } from 'components/share_qr_code';
import { useGlobalContext } from 'context/global';
import React, { FC, useState, useEffect } from 'react';
import { useAIContext, IAIShareInfoResponse } from '@apitable/ai';
import { Message, Skeleton, IconButton, Button, LinkButton, Switch, Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { CodeOutlined, LinkOutlined, CloseOutlined, NewtabOutlined, QrcodeOutlined, QuestionCircleOutlined } from '@apitable/icons';
import { getEnvVariables } from 'pc/utils/env';
import { WidgetEmbed } from '../../widget_embed';
import styles from './index.module.less';

export interface IShareProps {
  close?: () => void;
  visible?: boolean;
}

export const Share: FC<React.PropsWithChildren<IShareProps>> = ({ visible, close }) => {
  const globalContext = useGlobalContext();
  const [loading, setLoading] = useState(true);
  const [widgetEmbedVisible, setWidgetEmbedVisible] = useState(false);
  const [share, setShare] = useState<IAIShareInfoResponse | null>(null);
  const [sharing, setSharing] = useState(false);
  const [openPopconfirm, setOpenPopconfirm] = useState(false);
  const shareHost = `${window.location.protocol}//${window.location.host}/share/`;
  const { context } = useAIContext();
  const checkShare = async () => {
    try {
      setLoading(true);
      const ret = await context.api.shareInfo(context.data.id);
      setShare(ret.data);
      setLoading(false);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    checkShare();
  }, [visible]);

  const renderShareSwitchButton = () => {
    return (
      <div className={styles.shareToggle}>
        <Popconfirm
          open={openPopconfirm}
          overlayClassName={styles.deleteNode}
          title={t(Strings.link_failed_after_close_share_link)}
          onCancel={() => {
            setOpenPopconfirm(false);
          }}
          onOk={async () => {
            try {
              await context.api.shareClose(context.data.id);
              await checkShare();
              setOpenPopconfirm(false);
            } catch (error) {
              console.log(error);
            }
          }}
          type="danger"
        >
          <Switch checked onChange={() => setOpenPopconfirm(true)} />
        </Popconfirm>
        <Typography variant="h7" className={styles.shareToggleContent}>
          {t(Strings.publish_share_link_with_anyone)}
        </Typography>
        <Tooltip title={t(Strings.support)} trigger={'hover'}>
          <a href={getEnvVariables().WORKBENCH_NODE_SHARE_HELP_URL} rel="noopener noreferrer" target="_blank">
            <QuestionCircleOutlined currentColor />
          </a>
        </Tooltip>
      </div>
    );
  };

  const render = () => {
    if (loading) {
      return (
        <div className={styles.publish}>
          <Skeleton count={1} style={{ marginTop: 0 }} width="25%" height="24px" />
          <Skeleton count={1} style={{ marginTop: '28px' }} height="24px" />
          <Skeleton count={1} style={{ marginTop: '16px' }} height="24px" />
        </div>
      );
    }
    if (!share || !share.isEnabled) {
      return (
        <div className={styles.shareTips}>
          <div className={styles.title}>
            <Typography align="center" style={{ marginBottom: 8 }} variant="h6">
              {t(Strings.share_tips_title)}
            </Typography>
            <Typography align="left" variant="body3">
              {t(Strings.share_tips_ai)}
            </Typography>
          </div>
          <Button
            loading={sharing}
            style={{ width: 160 }}
            className={styles.shareOpenButton}
            color="primary"
            onClick={async () => {
              setSharing(true);
              try {
                await context.api.shareOpen(context.data.id);
                await checkShare();
                setSharing(false);
              } catch (error) {
                console.log(error);
              }
            }}
          >
            {t(Strings.publish)}
          </Button>
        </div>
      );
    }

    const copyLinkHandler = async () => {
      const shareText = t(Strings.workbench_share_link_template, {
        nickName: globalContext.context.user.nickName || t(Strings.friend),
        nodeName: context.data.name,
      });
      await navigator.clipboard.writeText(`${shareHost}${share.shareId} ${shareText}`);
      Message.success({ content: t(Strings.message_copy_link_successfully) });
    };

    return (
      <div className={styles.publish}>
        {renderShareSwitchButton()}
        <div className={styles.sharePerson}>
          <Typography className={styles.sharePersonContent} variant="body2">
            {t(Strings.get_ai_link_person_on_internet)}
          </Typography>
          {/* <DoubleSelect
          value={value}
          disabled={false}
          onSelected={(op) => handleShareAuthClick(op)}
          triggerCls={styles.doubleSelect}
          options={Permission}
        /> */}
        </div>
        <div className={styles.shareLink}>
          <div className={styles.inputContainer}>
            <input type="text" className={styles.link} value={shareHost + share.shareId} id={share.shareId} readOnly />
            <Tooltip title={t(Strings.preview)} placement="top">
              <IconButton
                icon={NewtabOutlined}
                onClick={() => {
                  window.open(`${shareHost}${share.shareId}`);
                }}
                variant="background"
                className={styles.inputButton}
              />
            </Tooltip>
          </div>
        </div>
        <div className={styles.inviteMore}>
          <LinkButton
            className={styles.inviteMoreMethod}
            underline={false}
            onClick={() => copyLinkHandler()}
            prefixIcon={<LinkOutlined currentColor />}
          >
            {t(Strings.share_copy_url_link)}
          </LinkButton>
          <LinkButton
            className={styles.inviteMoreMethod}
            underline={false}
            onClick={() => setWidgetEmbedVisible(true)}
            prefixIcon={<CodeOutlined currentColor />}
          >
            Embed
          </LinkButton>

          <Tooltip
            trigger="click"
            placement="left"
            showArrow={false}
            overlayInnerStyle={{ padding: 0, backgroundColor: 'transparent' }}
            overlay={
              <ShareQrCode
                url={`${shareHost}${share.shareId}`}
                // @ts-ignore
                user={globalContext.context.user}
                nodeName={context.name}
              />
            }
          >
            <LinkButton className={styles.inviteMoreMethod} underline={false} prefixIcon={<QrcodeOutlined currentColor />}>
              {t(Strings.share_qr_code_tips)}
            </LinkButton>
          </Tooltip>
          <WidgetEmbed
            visible={widgetEmbedVisible}
            hide={() => {
              setWidgetEmbedVisible(false);
            }}
            shareId={share.shareId}
          />
        </div>
      </div>
    );
  };

  return (
    <Modal
      title={
        <>
          <Typography variant="h6">{t(Strings.publish)}</Typography>
          <CloseOutlined onClick={close} />
        </>
      }
      className={styles.modal}
      closable={false}
      open={visible}
      width={500}
      onCancel={close}
      destroyOnClose
      footer={null}
      centered
    >
      {render()}
    </Modal>
  );
};
