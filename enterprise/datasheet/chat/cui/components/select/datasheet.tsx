import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { registerCUIComponent, CUIFormPanel, ICUIForm, ICUIChatProps, ISelectProps } from '@apitable/ai';
import { Typography } from '@apitable/components';
import { t, Strings, IReduxState, ConfigConstant } from '@apitable/core';
import { AddFilled } from '@apitable/icons';
import { DataSourceSelectorForNode } from 'pc/components/data_source_selector_enhanced/data_source_selector_for_node/data_source_selector_for_node';
import style from './index.module.less';

type Props = ISelectProps & ICUIChatProps;

export default function CUIFormSelectDatasheet(props: Props) {
  const { title, description, isComplete } = props;
  const rootId = useSelector((state: IReduxState) => state.catalogTree.rootId);
  const [panelVisible, setPanelVisible] = useState(false);
  const [isLoading, setIsLoading] = React.useState(false);

  return (
    <CUIFormPanel title={title} description={description}>
      <div
        className={style.selectDatasheet}
        onClick={() => {
          if (!isLoading && !isComplete) {
            setPanelVisible(true);
          }
        }}
      >
        <AddFilled size={32} />
        <Typography style={{ marginTop: 8 }} variant="body2" className={style.selectDatasheetDescription} align="center">
          {props.description || t(Strings.cui_select_datasheet_description)}
        </Typography>
      </div>
      {panelVisible && (
        <DataSourceSelectorForNode
          onHide={() => {
            setPanelVisible(false);
          }}
          requiredData={['datasheetId', 'viewId', 'nodeName']}
          permissionRequired={'manageable'}
          onChange={async (data) => {
            setIsLoading(true);
            console.log(data);
            await props.onSubmit({
              id: data.datasheetId,
              viewId: data.viewId,
              name: data.nodeName,
            });
            setIsLoading(false);
          }}
          nodeTypes={[ConfigConstant.NodeType.DATASHEET]}
          defaultNodeIds={{ folderId: rootId }}
        />
      )}
    </CUIFormPanel>
  );
}

registerCUIComponent('CUIFormSelectDatasheet', {
  component: CUIFormSelectDatasheet,
  getDisplayResultWithComponent:  (form: ICUIForm, data: any) => {
    console.log(data);
    return data.name;
  }
});
