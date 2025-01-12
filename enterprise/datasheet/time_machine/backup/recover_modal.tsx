import { TreeSelect } from 'antd';
import React, { useContext, useEffect, useState } from 'react';
import { Skeleton, Typography } from '@apitable/components';
import { Api, DatasheetApi, Navigation, Strings, t } from '@apitable/core';
import { ChevronDownOutlined } from '@apitable/icons';
import { BaseModal, Message } from 'pc/components/common';
import { Router } from 'pc/components/route_manager/router';
import { useCatalogTreeRequest, useRequest } from 'pc/hooks';
import { transformNodeTreeData, ISelectTreeNode } from 'pc/utils';
import { BackupContext } from './context';
import { IBackupRecoverModal } from './interface';
import styles from './style.module.less';

export const BackupRecoverModal = (props: IBackupRecoverModal) => {
  const { setRecoverTbdId, tbdId } = props;
  const [treeData, setTreeData] = useState<ISelectTreeNode[]>([]);
  const [nodeId, setNodeId] = useState('');
  const { datasheetId } = useContext(BackupContext);
  const [recovering, setRecovering] = useState(false);

  const { getNodeTreeReq } = useCatalogTreeRequest();
  const { loading, data: nodeTreeData } = useRequest(getNodeTreeReq);

  useEffect(() => {
    if (nodeTreeData) {
      setTreeData(transformNodeTreeData([nodeTreeData]));
      setNodeId(nodeTreeData.nodeId);
    }
    // eslint-disable-next-line
  }, [nodeTreeData]);

  const onLoadData = (treeNode: any) => {
    const { id } = treeNode.props;
    if (treeData.findIndex(item => item.pId === id) !== -1) {
      return new Promise<void>(resolve => {
        resolve();
      });
    }
    return new Promise<void>(async resolve => {
      const { data: result } = await Api.getChildNodeList(id);
      const { data } = result;
      setTreeData([...treeData, ...transformNodeTreeData(data)]);
      resolve();
    });
  };

  const handleOk = async () => {
    setRecovering(true);
    const res = await DatasheetApi.recoverDatasheetTablebundle(datasheetId, tbdId, nodeId, t(Strings.backup_recover_dst_name_suffix));
    setRecovering(false);
    if (res.data.success) {
      Message.success({
        content: t(Strings.backup_recover_success)
      });
      const nodeId = res.data?.data?.dstId;
      Router.push(Navigation.WORKBENCH, { params: { nodeId } });
    }
    setRecoverTbdId('');
  };
  const onChange = (value: string) => {
    setNodeId(value);
  };

  return (
    <BaseModal
      title={t(Strings.backup_recover_title)}
      onCancel={() => setRecoverTbdId('')}
      onOk={handleOk}
      className={styles.recoverModal}
      confirmLoading={recovering}
    >
      <div className={styles.recoverModalContent}>
        <Typography variant="h7">{t(Strings.backup_recover_modal_text)}</Typography>
        <div className={styles.recoverModalTree}>
          {loading && <Skeleton count={2} />}
          {
            treeData.length !== 0 && nodeId &&
            <TreeSelect
              treeDataSimpleMode
              style={{ width: '100%' }}
              dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
              suffixIcon={<ChevronDownOutlined />}
              value={nodeId}
              onChange={onChange}
              treeData={treeData}
              loadData={onLoadData}
              treeDefaultExpandedKeys={[nodeTreeData.nodeId]}
            />
          }
        </div>
      </div>
    </BaseModal>
  );
};