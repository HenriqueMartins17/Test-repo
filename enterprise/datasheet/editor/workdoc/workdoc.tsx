import { Drawer } from 'antd';
import { get } from 'lodash';
import Router from 'next/router';
import * as React from 'react';
import { useState } from 'react';
import { Strings, t } from '@apitable/core';
import { ScreenSize } from 'pc/components/common/component_display';
import { Popup } from 'pc/components/common/mobile/popup';
import { Modal } from 'pc/components/common/modal';
import { useResponsive } from 'pc/hooks';
import { IWorkdocProps, Status } from './interface';
import { WorkdocEditor } from './workdoc_editor';
import styles from './style.module.less';

export const Workdoc = (props: IWorkdocProps) => {
  const { onSave, editing, toggleEditing, cellValue } = props;

  const [title, setTitle] = useState<string>(get(cellValue, '0.title') || '');
  const [status, setStatus] = React.useState<Status>(Status.Connecting);
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  const handleSuccessClose = () => {
    const cellValueTitle = get(cellValue, '0.title');
    const documentId = get(cellValue, '0.documentId') || '';
    if (cellValueTitle !== title && status === Status.Connected) {
      onSave?.([
        {
          documentId,
          title,
        },
      ]);
    }
    // query remove recordId and fieldId
    const url = Router.asPath;
    const urlObj = new URL(url, window.location.origin);
    urlObj.searchParams.delete('recordId');
    urlObj.searchParams.delete('fieldId');
    Router.replace(urlObj.toString());
    toggleEditing && toggleEditing();
  };

  const onClose = (e: React.MouseEvent | React.KeyboardEvent) => {
    e.preventDefault();
    if (status !== Status.Connected) {
      Modal.warning({
        title: t(Strings.workdoc_unsave_title),
        content: t(Strings.workdoc_unsave_content),
        hiddenCancelBtn: false,
        cancelText: t(Strings.workdoc_unsave_cancel),
        okText: t(Strings.workdoc_unsave_ok),
        onOk: handleSuccessClose,
      });
      return;
    }
    handleSuccessClose();
  };

  if (isMobile) {
    return (
      <Popup className={styles.popup} height="90%" open={editing} onClose={onClose} destroyOnClose closable headerStyle={{ display: 'none' }}>
        <WorkdocEditor
          fieldId={props.fieldId}
          recordId={props.recordId}
          editing={editing}
          datasheetId={props.datasheetId}
          cellValue={cellValue}
          editable={props.editable}
          onSave={onSave}
          title={title}
          setTitle={setTitle}
          setStatus={setStatus}
          isMobile={isMobile}
          onClose={onClose}
          status={status}
        />
      </Popup>
    );
  }

  return (
    <Drawer
      className={styles.drawer}
      placement="right"
      height={'100%'}
      width={'90%'}
      open={editing}
      closable
      onClose={onClose}
      destroyOnClose
      headerStyle={{ display: 'none' }}
    >
      <WorkdocEditor
        fieldId={props.fieldId}
        recordId={props.recordId}
        editing={editing}
        datasheetId={props.datasheetId}
        cellValue={cellValue}
        editable={props.editable}
        onSave={onSave}
        title={title}
        setTitle={setTitle}
        setStatus={setStatus}
        isMobile={isMobile}
        onClose={onClose}
        status={status}
      />
    </Drawer>
  );
};
