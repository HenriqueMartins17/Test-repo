import classNames from 'classnames';
import dayjs from 'dayjs';
import { find, values } from 'lodash';
import { useEffect, useState } from 'react';
import * as React from 'react';
import { Skeleton, Typography } from '@apitable/components';
import { Strings, Api, t, Selectors, MemberType, OtherTypeUnitId, integrateCdnHost, Settings } from '@apitable/core';
import { AvatarSize, Avatar } from 'pc/components/common';
import { useAppSelector } from 'pc/store/react-redux';
import { IWorkdocInfo, IWorkdocInfoResponse } from './interface';
import styles from './styles.module.less';

export const CONST_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

export const WorkdocInfo = (props: IWorkdocInfo) => {
  const [documentData, setDocumentData] = useState<IWorkdocInfoResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const unitMap = useAppSelector((state) => Selectors.getUnitMap(state));

  useEffect(() => {
    const getDocumentData = async () => {
      setLoading(true);
      const response = await Api.getWorkdocInfo(props.documentId);

      const { data } = response.data;

      setDocumentData(data);
      setLoading(false);
    };

    getDocumentData();

  }, [props.documentId]);

  const creatorUnit = find(values(unitMap), { userId: documentData?.creatorUuid }) || {
    type: MemberType.Member,
    userId: OtherTypeUnitId.Alien,
    unitId: OtherTypeUnitId.Alien,
    avatar: integrateCdnHost(Settings.datasheet_unlogin_user_avatar.value),
    name: t(Strings.anonymous),
    isActive: true,
  };

  return (
    <div className={classNames(styles.workdocInfo, { [styles.open]: open })}>
      {loading ? (
        <div className={styles.loading}>
          <Skeleton width="38%" height="28px" />
          <Skeleton count={2} height="20px" />
        </div>
      ) : (
        <div>
          <Typography variant="h5" className={styles.infoTitle}>
            {t(Strings.workdoc_info)}
          </Typography>
          <Typography variant="body4" className={styles.infoDetails}>
            <div className={styles.infoDetailsItem}>
              <div className={styles.itemTitle}>{t(Strings.workdoc_info_creator)}</div>
              <div className={styles.avatarItem}>
                <Avatar
                  id={creatorUnit?.unitId || ''}
                  title={creatorUnit.nickName || creatorUnit.name}
                  src={documentData?.creatorAvatar || creatorUnit.avatar}
                  size={AvatarSize.Size20}
                  avatarColor={creatorUnit?.avatarColor}
                />
                <div className={styles.itemContent}>{documentData?.creatorName || creatorUnit.nickName || creatorUnit.name}</div>
              </div>
            </div>
            <div className={styles.infoDetailsItem}>
              <div className={styles.itemTitle}>{t(Strings.workdoc_info_create_time)}</div>
              <div className={styles.itemContent}>{dayjs.tz(documentData?.createdAt ?? new Date()).format(CONST_DATETIME_FORMAT)}</div>
            </div>
            {/*<div className={styles.infoDetailsItem}>*/}
            {/*  <div className={styles.itemTitle}>{t(Strings.workdoc_info_last_modify_people)}</div>*/}
            {/*  <div className={styles.avatarItem}>*/}
            {/*    <Avatar*/}
            {/*      id={lastModifiedUnit?.unitId || ''}*/}
            {/*      title={documentData?.lastModifiedBy || ''}*/}
            {/*      src={documentData?.lastModifiedAvatar}*/}
            {/*      avatarColor={lastModifiedUnit?.avatarColor}*/}
            {/*      size={AvatarSize.Size20}*/}
            {/*    />*/}
            {/*    <div className={documentData?.lastModifiedBy}>{documentData?.creatorName}</div>*/}
            {/*  </div>*/}
            {/*</div>*/}
            {/*<div className={styles.infoDetailsItem}>*/}
            {/*  <div className={styles.itemTitle}>{t(Strings.workdoc_info_last_modify_time)}</div>*/}
            {/*  <div className={styles.itemContent}>{dayjs.tz(documentData?.lastModifiedAt ?? new Date()).format(CONST_DATETIME_FORMAT)}</div>*/}
            {/*</div>*/}
          </Typography>
        </div>
      )}
    </div>
  );
};
