import { Col, Row } from 'antd';
import React from 'react';
import { useSelector } from 'react-redux';
import { useAIContext } from '@apitable/ai';
import { IconButton, Typography, useThemeColors } from '@apitable/components';
import { ConfigConstant, Navigation, Strings, t } from '@apitable/core';
import { AddFilled, NewtabOutlined, ToggleOutlined } from '@apitable/icons';
// import { getNodeIcon } from 'pc/components/catalog/tree/node_icon';
// import { SecondConfirmType } from 'pc/components/datasheet_search_panel';
// import { Router } from 'pc/components/route_manager/router';
import { SelectForm } from './select_form';
import styles from './style.module.less';

const SelectFormWidget = function () {
  // const color = useThemeColors();
  // const { context } = useAIContext();
  // const treeNodesMap = useSelector((state) => state.catalogTree.treeNodesMap);
  // const rootId = useSelector((state) => state.catalogTree.rootId);

  // const openNodeTab = (nodeId?: string) => {
  //   if (nodeId) Router.newTab(Navigation.WORKBENCH, { params: { nodeId } });
  // };

  // return props.value && treeNodesMap[props.value] ? (
  //   <Row className={styles.selectDstItem}>
  //     <Col className={styles.nodeWrapper}>
  //       {/* {getNodeIcon(undefined, ConfigConstant.NodeType.FORM)} */}
  //       null
  //     </Col>
  //     <Col flex={1}>
  //       <div style={{ display: 'flex', alignItems: 'center' }}>
  //         <Typography variant={'body4'} color={color.textCommonPrimary} ellipsis style={{ maxWidth: context.data.dataSourcesUpdated ? 330 : 420 }}>
  //           {treeNodesMap[props.value]?.nodeName}
  //         </Typography>
  //       </div>
  //     </Col>
  //     <Col style={{ display: 'flex', alignItems: 'center', marginRight: 8 }}>
  //       <IconButton icon={NewtabOutlined} onClick={() => openNodeTab(props.value)} />
  //     </Col>
  //     <Col style={{ display: 'flex', alignItems: 'center' }}>
  //       <SelectForm rootId={rootId} secondConfirmType={SecondConfirmType.AIForm} onChange={(event) => props.onChange(event)}>
  //         {(onClick) => <IconButton onClick={onClick} icon={ToggleOutlined} />}
  //       </SelectForm>
  //     </Col>
  //   </Row>
  // ) : (
  //   <SelectForm rootId={rootId} secondConfirmType={SecondConfirmType.AIForm} onChange={(event) => props.onChange(event)}>
  //     {(onClick) => (
  //       <div className={styles.selectDst} onClick={onClick}>
  //         <AddFilled />
  //         <Typography style={{ marginLeft: 8 }} color={color.textCommonTertiary} variant="body3">
  //           {t(Strings.ai_select_form)}
  //         </Typography>
  //       </div>
  //     )}
  //   </SelectForm>
  // );
  return <div>whatbbbb</div>;
};

export const widgets = {
  SelectFormWidget: SelectFormWidget,
};
