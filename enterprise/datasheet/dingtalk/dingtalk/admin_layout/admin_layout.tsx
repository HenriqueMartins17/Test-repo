import { Table } from 'antd';
import axios from 'axios';
import dayjs from 'dayjs';
import Image from 'next/image';
import React, { FC } from 'react';
import { LinkButton, Typography, useThemeColors } from '@apitable/components';
import { IMember, Settings, Strings, t } from '@apitable/core';
import { expandUnitModal, SelectUnitSource } from 'pc/components/catalog/permission_settings/permission/select_unit_modal';
import { Avatar, AvatarSize, AvatarType, Modal } from 'pc/components/common';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import BronzeImg from 'static/icon/space/space_img_bronze.png';
import GradeEnterpriseImg from 'static/icon/space/space_img_enterprise.png';
import SilverImg from 'static/icon/space/space_img_silver.png';
import { Copyright, Header } from '../../../dingtalk';
import { AdminChangeModal } from './admin_change_modal';
import { AdminName, IAdminNameProps } from './admin_name';
import styles from './style.module.less';

const LevelConfig = {
  bronze: {
    img: BronzeImg,
    text: t(Strings.bronze_grade)
  },
  silver: {
    img: SilverImg,
    text: t(Strings.silver_grade)
  },
  dingtalk_base: {
    img: BronzeImg,
    text: t(Strings.dingtalk_basic)
  },
  dingtalk_basic: {
    img: SilverImg,
    text: t(Strings.dingtalk_standard)
  },
  dingtalk_standard: {
    img: SilverImg,
    text: t(Strings.dingtalk_standard)
  },
  dingtalk_enterprise: {
    img: GradeEnterpriseImg,
    text: t(Strings.dingtalk_enterprise)
  },
  feishu_base: {
    img: BronzeImg,
    text: t(Strings.feishu_base)
  },
  feishu_standard: {
    img: SilverImg,
    text: t(Strings.feishu_standard)
  },
  feishu_enterprise: {
    img: GradeEnterpriseImg,
    text: t(Strings.feishu_enterprise)
  },
  wecom_base: {
    img: BronzeImg,
    text: t(Strings.wecom_base)
  },
  wecom_standard: {
    img: SilverImg,
    text: t(Strings.wecom_standard)
  },
  wecom_enterprise: {
    img: GradeEnterpriseImg,
    text: t(Strings.wecom_enterprise)
  }
};

interface ISpace {
  deadline: string;
  mainAdminUserName: string;
  mainAdminUserAvatar: string;
  product: string;
  spaceId: string;
  spaceName: string;
  spaceLogo: string;
}

export interface IAdminData {
  avatar: string;
  tenantKey: string;
  tenantName: string;
  spaces: ISpace[];
}

interface IAdminLayoutProps {
  data: IAdminData;
  config: {
    adminTitle: string;
    adminDesc: string;
    helpLink: string;
  };
  onChange: (spaceId: string, memberId: string) => void;
}

export const AdminLayout: FC<IAdminLayoutProps> = (props) => {
  const colors = useThemeColors();
  const { data, config, onChange } = props;
  const { tenantKey, tenantName, avatar, spaces } = data;
  const { adminTitle, adminDesc } = config;

  const handleSubmit = (spaceId: string, mainAdminUserName: string, values: IMember[]) => {
    if (values.length < 0) return;
    const memberId = values[0]?.memberId;
    const memberName = values[0]?.originName || values[0]?.memberName;
    Modal.confirm({
      type: 'warning',
      title: t(Strings.feishu_configure_change_space_master_modal_title),
      className: styles.changeConfirmModal,
      onOk: () => onChange(spaceId, memberId!),
      content: (
        <AdminChangeModal
          memberName={memberName!}
          spaceId={spaceId}
          mainAdminUserName={mainAdminUserName}
        />
      )
    });
  };

  const onClick = (spaceId: string, mainAdminUserName: string, mainAdminUserId?: string) => {
    axios.defaults.headers['X-Space-Id'] = spaceId;
    expandUnitModal({
      source: SelectUnitSource.Admin,
      onSubmit: values => handleSubmit(spaceId, mainAdminUserName, values as IMember[]),
      isSingleSelect: true,
      hiddenInviteBtn: true,
      disableIdList: mainAdminUserId ? [mainAdminUserId] : undefined,
      spaceId,
    });
  };

  const columns = [
    {
      title: t(Strings.space_name),
      dataIndex: 'spaceName',
      key: 'spaceName',
      render: (value: string, record: IAdminNameProps) =>
        (<span style={{ display: 'flex' }}>
          <Avatar src={record.spaceLogo} size={AvatarSize.Size20} id={record.spaceId} title={value} type={AvatarType.Space} />
          <span style={{ marginLeft: '4px' }}>{value}</span>
        </span>)
    },
    {
      title: t(Strings.main_admin_name),
      dataIndex: 'mainAdminUserName',
      key: 'mainAdminUserName',
      render: (value: string, record: IAdminNameProps) => <AdminName {...record} value={value} />
    },
    {
      title: t(Strings.space_admin_level),
      dataIndex: 'product',
      key: 'product',
      render: (value: string) => {
        return (
          <span className={styles.levelWrapper}>
            <span className={styles.level}>
              <Image src={LevelConfig[value.toLowerCase()]?.img} width={24} height={24} alt="" />
            </span>
            {LevelConfig[value.toLowerCase()]?.text}
          </span>
        );
      }
    },
    {
      title: t(Strings.expiration_time_of_space),
      dataIndex: 'deadline',
      key: 'deadline',
      render: (value: string) => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : t(Strings.EXPIRATION_NO_BILLING_PERIOD)
    },
    {
      title: t(Strings.operate),
      dataIndex: 'product',
      key: 'product',
      render: (_value: string, record: IAdminNameProps) => {
        return (
          <div style={{ display: 'flex' }}>
            <LinkButton
              underline={false}
              style={{ marginRight: '8px' }}
              onClick={() => onClick(record.spaceId, record.mainAdminUserName, record?.mainAdminUserId)}
            >
              {t(Strings.change_main_admin)}
            </LinkButton>
            <LinkButton
              underline={false}
              href={window.location.origin + `/space/${record.spaceId}/workbench`}
              target='_blank'
            >
              {t(Strings.entry_space)}
            </LinkButton>
          </div>
        );
      }
    },
  ];

  return (
    <div className={styles.adminContainer}>
      <div className={styles.scrollWrap}>
        <Header>
          <div className={styles.headerRight}>
            <LinkButton
              underline={false}
              onClick={() => {
                if (window.location.href.includes('dingtalk')) {
                  navigationToUrl(Settings.integration_dingtalk_help_url.value, { clearQuery: true });
                  return;
                }
                if (window.location.href.includes('feishu')) {
                  navigationToUrl(Settings.integration_feishu_help_url.value, { clearQuery: true });
                  return;
                }
              }}
              target='_blank'
              color={colors.defaultBg}
              className={styles.helper}
            >
              {t(Strings.help_center)}
            </LinkButton>
            <Avatar
              src={avatar}
              size={AvatarSize.Size24}
              id={tenantKey}
              title={tenantName}
            />
            <Typography
              className={styles.name}
              variant='body2'
              color={colors.thirdLevelText}
              align='center'
            >
              {tenantName}
            </Typography>
          </div>
        </Header>
        <div className={styles.contentWrap}>
          <div className={styles.content}>
            <div className={styles.titleWrapper}>
              <Typography variant='h3' className={styles.title}>
                {adminTitle}
              </Typography>
              <Typography variant='body2' className={styles.msg}>
                {adminDesc}
              </Typography>
            </div>
            <Table
              dataSource={spaces as any}
              columns={columns}
              pagination={false}
            />
          </div>
        </div>
        <Copyright />
      </div>
    </div>
  );
};
