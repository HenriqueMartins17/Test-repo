import { Modal as ModalComponent, Spin } from 'antd';
import { useEffect } from 'react';
import { deepPurple } from '@apitable/components';
import { ConfigConstant, Selectors, StoreActions, Strings, t, IViewProperty } from '@apitable/core';
import { LoadingOutlined } from '@apitable/icons';
import { store } from 'pc/store';
import { exportDatasheetBase } from 'pc/utils';

const listenerIframeMessage = {
  exportData: ({ nodeId, viewId, fileType = 'xlsx' }: { nodeId: string; fileType?: 'csv' | 'xlsx' | 'png'; viewId?: string }) => {
    if (!nodeId.startsWith('dst')) {
      throw new Error('Can only export data from the datasheet.');
    }
    const state = store.getState();

    const permission = Selectors.getPermissions(state, nodeId, undefined);

    if (!permission.exportable) {
      throw new Error('No permission to export data.');
    }

    let view: IViewProperty | undefined;

    if (viewId && viewId.startsWith('viw')) {

      const snapshot = Selectors.getSnapshot(state, nodeId);
      if (!snapshot) {
        throw new Error('No data found to export');
      }
      view = Selectors.getViewById(snapshot, state.pageParams.viewId!);
    }

    if (fileType === 'png') {
      if (!view) {
        throw new Error('Only view data can export images');
      }
      ModalComponent.success({
        icon: null,
        title: <Spin style={{ width: '100%' }} indicator={<LoadingOutlined size={16} color={deepPurple[500]} className="circle-loading" />} />,
        content: t(Strings.export),
        width: 180,
        style: {
          textAlign: 'center'
        },
        centered: true,
        okButtonProps: {
          style: {
            display: 'none'
          }
        }
      });
      setTimeout(() => {
        store.dispatch(StoreActions.activeExportViewId(view!.id, nodeId!));
      }, 200);
      return;
    }

    return exportDatasheetBase(nodeId, fileType === 'xlsx' ? ConfigConstant.EXPORT_TYPE_XLSX : ConfigConstant.EXPORT_TYPE_CSV, { view });
  }
};

export const useListenerIframeMessage = () => {

  useEffect(() => {

    const listenerIframe = (event: any) => {
      const {
        data: { msg: eventName, data },
      } = event;

      if (!listenerIframeMessage.hasOwnProperty(eventName)) {
        return;
      }

      const func = listenerIframeMessage[eventName];

      if (func) {
        console.log('Execute iframe listening event.');
      }

      func(data).then(() => {
        window.parent.postMessage({
          message: 'triggerEventResult',
          data: {
            eventName: eventName,
            success: true,
          }
        }, '*');
      }).catch((e: any) => {
        window.parent.postMessage({
          message: 'triggerEventResult',
          data: {
            eventName: eventName,
            success: false,
            message: e.message
          }
        }, '*');
      });
    };

    window.addEventListener('message', listenerIframe);
    return () => {
      window.removeEventListener('message', listenerIframe);
    };
  }, []);
};
