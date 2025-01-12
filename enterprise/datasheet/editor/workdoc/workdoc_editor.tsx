import { Tooltip } from 'antd';
import classNames from 'classnames';
import isHotkey from 'is-hotkey';
import dynamic from 'next/dynamic';
import * as React from 'react';
import { IconButton, Skeleton } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { CloseOutlined, InfoCircleOutlined } from '@apitable/icons';
import { IWorkdocEditorProps, Status } from './interface';
import { WorkdocCollaborators } from './workdoc_collaborators';
import styles from './style.module.less';

const CollaborationEditorNoSSR = dynamic(() => import('./collaboration_editor'), { ssr: false });
const PREVENT_KEYS = ['tab'];

export const WorkdocEditor = (props: IWorkdocEditorProps) => {
  const { status, title, onClose, ...rest } = props;

  const [collaborators, setCollaborators] = React.useState<string[]>([]);
  const [infoOpen, setInfoOpen] = React.useState(false);

  return (
    <div
      className={styles.workdocEditor}
      tabIndex={0}
      onKeyDown={(e) => {
        const isPrevent = PREVENT_KEYS.some((key) => isHotkey(key, e));
        if (isPrevent) {
          e.preventDefault();
          e.stopPropagation();
        }
      }}
    >
      <header>
        {status === Status.Connecting ? (
          <Skeleton width="20%" height="24px" className={styles.skeleton} />
        ) : (
          <div className={styles.title}>{title || t(Strings.workdoc_unnamed)}</div>
        )}
        <div className={styles.right}>
          <div className={styles.status}>
            {status === Status.Connecting && <div className={styles.connecting}>{t(Strings.workdoc_ws_connecting)}</div>}
            {status === Status.Reconnecting && <div className={styles.reconnecting}>{t(Strings.workdoc_ws_reconnecting)}</div>}
            {status === Status.Disconnected && <div className={styles.disconnected}>{t(Strings.workdoc_ws_disconnected)}</div>}
          </div>
          {!rest.isMobile && <WorkdocCollaborators collaborators={collaborators} />}
          <Tooltip title={t(Strings.workdoc_info)}>
            <IconButton
              icon={InfoCircleOutlined}
              shape="square"
              className={classNames(styles.infoIconButton, { [styles.active]: infoOpen })}
              onClick={() => setInfoOpen(!infoOpen)}
            />
          </Tooltip>
          <div id="workdocDownload" />
          <IconButton icon={CloseOutlined} shape="square" className={styles.iconButton} onClick={onClose} />
        </div>
      </header>
      <CollaborationEditorNoSSR {...rest} status={status} infoOpen={infoOpen} setCollaborators={setCollaborators} collaborators={collaborators} />
    </div>
  );
};
