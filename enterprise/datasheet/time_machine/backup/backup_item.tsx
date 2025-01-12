import { Dropdown, Menu } from 'antd';
import classNames from 'classnames';
import dayjs from 'dayjs';
import * as React from 'react';
import { CSSProperties, useContext, useState } from 'react';
import { colorVars, IconButton, Typography } from '@apitable/components';
import {
  DatasheetApi,
  fastCloneDeep,
  IDatasheetTablebundles,
  PREVIEW_DATASHEET_BACKUP,
  PREVIEW_DATASHEET_ID,
  StoreActions,
  Strings,
  t,
} from '@apitable/core';
import { DeleteOutlined, EditOutlined, MoreStandOutlined, ReloadOutlined } from '@apitable/icons';
// eslint-disable-next-line no-restricted-imports
import { Avatar, AvatarSize, Message, Modal, Tooltip } from 'pc/components/common';
import { useCatalogTreeRequest } from 'pc/hooks';
import { useAppDispatch } from 'pc/hooks/use_app_dispatch';
import { KeyCode, stopPropagation } from 'pc/utils';
import { BackupContext } from './context';
import styles from './style.module.less';

const NAME_MIN_LEN = 1;
const NAME_MAX_LEN = 100;

export const BackupItem = (props: IDatasheetTablebundles) => {
  const { name, createdAt, deletedAt, tbdId, creatorInfo, isDeleted, expiredAt, statusCode, deleteInfo } = props;
  const expired = dayjs().isAfter(dayjs(Number(expiredAt)));
  const creating = statusCode === 0;

  const userInfo = isDeleted ? deleteInfo : creatorInfo;
  const itemAt = isDeleted ? deletedAt : createdAt;

  const { 
    setData, data, datasheetId, curPreview, setCurPreview,
    setRecoverTbdId,
    curDatasheet, type
  } = useContext(BackupContext);
  const { checkNodeNumberLimit } = useCatalogTreeRequest();
  const manageable = curDatasheet?.permissions.manageable;
  const editable = curDatasheet?.permissions.editable;
  const dispatch = useAppDispatch();

  const disabled = isDeleted || expired || creating || !editable;

  const [rightStyle, setRightStyle] = useState<CSSProperties>({});
  const [editing, setEditing] = useState(false);
  const [newName, setNewName] = useState(name);
  const [errMsg, setErrMsg] = useState('');

  const handleDelete = () => {
    Modal.warning({
      title: t(Strings.backup_delete_title),
      content: t(Strings.backup_delete_text),
      hiddenCancelBtn: false,
      onOk: async () => {
        const res = await DatasheetApi.deleteDatasheetTablebundle(datasheetId, tbdId);
        if (res.data.success) {
          const tablebundlesRlt = await DatasheetApi.getDatasheetTablebundles(datasheetId, tbdId);
          const newtablebundles = tablebundlesRlt.data.data;
          setData(data.map(d => d.tbdId === tbdId ? newtablebundles[0] : d));
          if (curPreview === tbdId) {
            dispatch(StoreActions.resetDatasheet(PREVIEW_DATASHEET_ID));
            setCurPreview(undefined);
          }
          Message.success({
            content: t(Strings.backup_success_delete)
          });
        }
      }
    });
  };

  const handlePreview = async () => {
    if (disabled || curPreview === tbdId) return;
    setCurPreview(tbdId);
    const res = await DatasheetApi.previewDatasheetTablebundle(datasheetId, tbdId);
    if (res.data.success) {
      const data = res.data?.data;
      const cloneDatasheet = fastCloneDeep(curDatasheet)!;
      try {
        dispatch(StoreActions.receiveDataPack({
          snapshot: data.snapshot,
          datasheet: {
            ...cloneDatasheet,
            id: PREVIEW_DATASHEET_ID,
            permissions: { ...cloneDatasheet.permissions, editable: false },
            preview: name,
            type: PREVIEW_DATASHEET_BACKUP
          }
        }, { isPartOfData: false }));
      } catch (error) {
        console.log(error);
      }
    }
  };

  const renameNode = async (_name: string) => {
    const res = await DatasheetApi.updateDatasheetTablebundle(datasheetId, tbdId, _name);
    if (res.data.success) {
      setData(data.map(d => d.tbdId === tbdId ? { ...d, name: _name } : d));
      Message.success({
        content: t(Strings.backup_name_success_update)
      });
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    setNewName(inputValue);
    if (inputValue.length < NAME_MIN_LEN || inputValue.length > NAME_MAX_LEN) {
      setErrMsg(t(Strings.name_length_err));
      return;
    }

    if (errMsg) {
      setErrMsg('');
    }
  };

  const resetName = () => {
    setNewName(name);
    setEditing(false);
  };

  const rename = (isReset?: boolean) => {
    if (!newName || errMsg) {
      if (isReset) resetName();
      return;
    }
    const value = newName.trim();
    if (value === name) {
      setEditing(false);
      return;
    }
    renameNode(value);
    setEditing(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    switch (e.keyCode) {
      case KeyCode.Enter:
        rename();
        break;
      case KeyCode.Esc:
        resetName();
        (e.target as HTMLInputElement).blur();
    }
  };
  
  const menu=(<Menu>
    <Menu.Item
      key="rename"
      icon={<EditOutlined color={colorVars.textCommonTertiary} />}
      onClick={() => setEditing(true)}
      disabled={disabled || !manageable}
    >
      {t(Strings.rename)}
    </Menu.Item>
    <Menu.Item
      key="recover"
      icon={<ReloadOutlined color={colorVars.textCommonTertiary} />}
      onClick={() => {
        const isLimit = checkNodeNumberLimit(type);
        if (!isLimit) {
          setRecoverTbdId(tbdId);
        }
      }}
      disabled={disabled || !manageable}
    >
      {t(Strings.backup_action_recover)}
    </Menu.Item>
    <Menu.Item
      key="delete"
      icon={<DeleteOutlined color={colorVars.textCommonTertiary} />}
      onClick={handleDelete}
      disabled={disabled || !manageable}
    >
      {t(Strings.backup_action_delete)}
    </Menu.Item>
  </Menu>);
  return (
    <Dropdown overlay={menu} trigger={['contextMenu']}>
      <div 
        className={classNames(styles.backupItem, curPreview === tbdId && styles.active, disabled && styles.disabled)} 
        onClick={handlePreview}
      >
        <div className={styles.left}/>
        <div className={styles.middle}>
          {!disabled && editing ? (
            <Tooltip title={errMsg} open={Boolean(errMsg)}>
              <input
                value={newName}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                onBlur={() => rename(true)}
                autoFocus
                spellCheck='false'
              />
            </Tooltip>
          ) : !disabled ? (
            <Typography ellipsis variant="h8" className={styles.title}>{name}</Typography>
          ) : (
            <div className={styles.middleTop}>
              <Typography ellipsis variant="h8" className={styles.title}>{name}</Typography>
              <div className={styles.deleted}>
                {isDeleted ? t(Strings.backup_deleted) : creating ? t(Strings.backup_creating) : t(Strings.expired)}
              </div>
            </div>
          )}
          <div className={styles.time}>
            <span className={styles.timeLabel}>{t(Strings.backup_create_time)}</span>
            {dayjs.tz(itemAt).format('YYYY-MM-DD HH:mm:ss')}
          </div>
          <div className={styles.time}>
            <span className={styles.timeLabel}>{t(Strings.backup_expired_time)}</span>
            {dayjs.tz(Number(expiredAt)).format('YYYY-MM-DD HH:mm:ss')}
          </div>
          <div className={styles.middleBottom}>
            <Avatar
              src={userInfo?.avatar || ''}
              avatarColor={userInfo?.color}
              size={AvatarSize.Size20}
              id={userInfo?.uuid || ''}
              title={userInfo?.nikeName || ''}
            />
            <Typography className={styles.name} ellipsis variant="body4">
              {userInfo?.nikeName}
            </Typography>
          </div>
        </div>
        {!disabled && <div className={styles.right} style={rightStyle} onClick={stopPropagation}>
          <Dropdown overlay={menu} trigger={['click']} onOpenChange={open => setRightStyle(open ? { display: 'block' } : {})}>
            <IconButton
              component="button"
              icon={MoreStandOutlined}
              shape="square"
            />
          </Dropdown>
        </div>}
      </div>
    </Dropdown>
  );
};