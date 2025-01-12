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
import React, { FC } from 'react';
import { useSelector } from 'react-redux';
import { IReduxState } from '@apitable/core';
import { ShareContent } from 'pc/components/catalog/share_node/share_content';
import { ComponentDisplay, ScreenSize } from 'pc/components/common/component_display';
import { Modal } from 'pc/components/common/modal/modal/modal';

export interface IShareProps {
  nodeId: string;
  onClose?: () => void;
  isTriggerRender?: boolean;
}

export const Share: FC<React.PropsWithChildren<IShareProps>> = ({ nodeId, onClose }) => {
  const treeNodesMap = useSelector((state: IReduxState) => state.catalogTree.treeNodesMap);

  const data = {
    nodeId: nodeId,
    type: treeNodesMap[nodeId]?.type,
    icon: treeNodesMap[nodeId]?.icon,
    name: treeNodesMap[nodeId]?.nodeName,
  };

  return (
    data.nodeId ? (
      <ComponentDisplay minWidthCompatible={ScreenSize.md}>
        <Modal
          closable={false}
          visible
          width={500}
          bodyStyle={{ padding: 0 }}
          onCancel={onClose}
          destroyOnClose
          footer={null}
          centered
        >
          <ShareContent data={data} defaultActiveKey="Publish" />
        </Modal>
      </ComponentDisplay>
    ):<></>
  );
};
