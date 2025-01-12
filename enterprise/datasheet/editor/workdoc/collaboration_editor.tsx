import { HocuspocusProvider } from '@hocuspocus/provider';
import Collaboration from '@tiptap/extension-collaboration';
import CollaborationCursor from '@tiptap/extension-collaboration-cursor';
import { useUnmount } from 'ahooks';
import classNames from 'classnames';
import { compact, get, uniqBy } from 'lodash';
import * as React from 'react';
import { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import { Skeleton } from '@apitable/components';
import { getNewId, IDPrefix, Strings, t } from '@apitable/core';
import { getAvatarRandomColor, Message } from 'pc/components/common';
import { useAppSelector } from 'pc/store/react-redux';
import { getCookie } from 'pc/utils';
import { getAnonymousId } from 'pc/utils/get_anonymous_Id';
import { getSocialWecomUnitName } from '../../home';
import { EditorCore } from '../editor_core';
import { ICollaborationEditor, Status } from './interface';
import styles from './style.module.less';

const CollaborationEditorBase = (props: ICollaborationEditor) => {
  const {
    onSave,
    datasheetId,
    editing,
    recordId,
    fieldId,
    status,
    cellValue,
    setTitle,
    setStatus,
    editable,
    formId,
    mount,
    isMobile,
    expandable = true,
    infoOpen,
    setCollaborators,
  } = props;

  const mounted = props.hasOwnProperty('mount') ? mount : true;
  const [provider, setProvider] = useState<HocuspocusProvider | null>(null);
  const userInfo = useSelector((state) => state.user.info);
  const spaceInfo = useSelector((state) => state.space.curSpaceInfo);
  const shareId = useAppSelector((state) => state.pageParams.shareId);
  const mirrorId = useAppSelector((state) => state.pageParams.mirrorId);
  const embedId = useAppSelector((state) => state.pageParams.embedId);

  const realMemberName = useMemo(() => {
    if (!userInfo) return '';
    const { memberName, isMemberNameModified } = userInfo;
    return (
      getSocialWecomUnitName?.({
        name: memberName,
        isModified: isMemberNameModified,
        spaceInfo,
      }) || memberName
    );
  }, [userInfo, spaceInfo]);

  const createProvider = (docId) => {
    // Common logic for creating a new HocuspocusProvider instance
    const token = getCookie('XSRF-TOKEN');
    const wsProtocol = window.location.protocol === 'http:' ? 'ws:' : 'wss:';
    const param = recordId ? { recordId } : formId ? { formId } : {};
    const shareParam = shareId ? { shareId } : {};
    const mirrorParam = mirrorId ? { mirrorId } : {};
    const embedParam = embedId ? { embedId } : {};
    const host = process.env.NEXT_PUBLIC_DOCUMENT_WS_HOST || window.location.host;

    return new HocuspocusProvider({
      url: `${wsProtocol}//${host}/document`,
      name: docId,
      token,
      parameters: {
        resourceId: datasheetId,
        documentType: 0,
        fieldId: fieldId,
        title: get(cellValue, '0.title', ''),
        ...param,
        ...shareParam,
        ...mirrorParam,
        ...embedParam,
      },
      preserveConnection: false,
      onConnect: () => {
        console.log(`Connected to ${docId}`);
        setStatus?.(Status.Connected);
      },
      onAuthenticationFailed: (data) => {
        Message.error({ content: t(Strings.workdoc_authentication_failed) });
        console.log(`Authentication failed for ${docId}`, data);
        setStatus?.(Status.Disconnected);
      },
      onDisconnect: () => {
        console.log(`Disconnected from ${docId}`);
        setStatus?.(Status.Disconnected);
      },
      onClose: ({ event }) => {
        console.log(`Closed ${docId}`, event);
        setStatus?.(Status.Disconnected);
      },
      onAwarenessUpdate: (data) => {
        if (setCollaborators) {
          const _collaborators = uniqBy(data.states.filter(state => Boolean(state.user)), 'user.unitId').map((state) =>
            state.user?.unitId || getAnonymousId(IDPrefix.WorkDocAonymousId)
          );
          setCollaborators(compact(_collaborators));
        }
      },
    });
  };

  const initializeProvider = () => {
    const docId = get(cellValue, '0.documentId') || getNewId(IDPrefix.Document);
    const _provider = createProvider(docId);
    setProvider(_provider);

    if (docId && editable && cellValue === null) {
      onSave?.([{ documentId: docId, title: '' }]);
    }
  };

  const disconnectProvider = () => {
    if (provider) {
      provider.disconnect();
    }
  };

  useEffect(() => {
    // Function to handle the online event
    const handleOnline = () => {
      console.log('System is back online. Reconnecting...');
      initializeProvider();
    };

    // Function to handle the offline event
    const handleOffline = () => {
      console.log('System is offline. Disconnecting...');
      setStatus?.(Status.Reconnecting);
      disconnectProvider();
    };

    // Add event listeners
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Cleanup function to remove event listeners
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useUnmount(() => {
    disconnectProvider();
  });

  useEffect(() => {
    if (!fieldId || !editing || provider || !mounted) {
      return;
    }
    initializeProvider();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [recordId, fieldId, editing, cellValue, editable, formId, mounted, datasheetId, setStatus, onSave, provider]);

  return (
    <div className={styles.workdocEditorContent} id="workdocEditorContent">
      {provider && mounted && status !== Status.Connecting ? (
        <EditorCore
          documentId={get(cellValue, '0.documentId', '')}
          className={classNames('editor', styles.editor)}
          setTitle={setTitle}
          editable={!isMobile && editable}
          expandable={!isMobile && expandable}
          extensions={[
            Collaboration.configure({
              document: provider?.document,
            }),
            CollaborationCursor.configure({
              provider,
              user: {
                name: realMemberName,
                unitId: userInfo?.unitId || '',
                color: getAvatarRandomColor(userInfo?.memberId || ''),
              },
            }),
          ]}
          infoOpen={infoOpen}
        />
      ) : (
        <div className={styles.loading}>
          <Skeleton width="38%" height="40px" />
          <Skeleton count={3} height="24px" />
          <Skeleton width="61%" height="24px" />
        </div>
      )}
    </div>
  );
};

const CollaborationEditor = React.memo(CollaborationEditorBase);

export default CollaborationEditor;
