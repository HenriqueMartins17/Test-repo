import React, { useState } from 'react';
import { IAISourceDatasheet } from '@apitable/ai';
import { ConfigConstant } from '@apitable/core';
// import { DataSourceSelectorForNode } from 'pc/components/data_source_selector_enhanced/data_source_selector_for_node/data_source_selector_for_node';
// import { SearchPanel, SecondConfirmType } from 'pc/components/datasheet_search_panel';

interface ISelectDataSheetProps {
  value?: IAISourceDatasheet[];
  onChange?: (data: IAISourceDatasheet[]) => void;
  children: (onClick: () => void) => React.ReactNode;
  rootId: string;
  secondConfirmType: any;
}

interface ISelectDataSheetProcess {
  resolve: (value: any) => void;
  reject: (reason?: any) => void;
  activeDatasheetId: string;
}

export const SelectDataSheet: React.FC<ISelectDataSheetProps> = ({ rootId, value, secondConfirmType, children, onChange }) => {
  const [selectDataSheetProcess, setSelectDataSheetProcess] = useState<ISelectDataSheetProcess | null>(null);

  const selectDatasheet = (activeDatasheetId?: string): Promise<any> => {
    return new Promise((resolve, reject) => {
      setSelectDataSheetProcess({
        resolve,
        reject,
        activeDatasheetId: activeDatasheetId || '',
      });
    });
  };

  const handelClick = async () => {
    // activeDatasheetId
    // 组件有BUG 会导致 meta 数据是空 晚些处理
    const res = await selectDatasheet(value?.[0]?.nodeId);
    // if (secondConfirmType === SecondConfirmType.AIForm) {
    //   onChange && onChange(res.formId);
    // } else {
    const view = res.meta.views[0];
    onChange &&
      onChange([
        {
          nodeId: res.datasheetId,
          nodeName: res.nodeName,
          nodeType: res.secondConfirmType,
          setting: {
            viewId: view.id,
            rows: view.rows.length,
            fields: view.columns,
          },
        },
      ]);
    // }
  };

  const defaultNodeIds =
    // secondConfirmType === SecondConfirmType.AIForm
    //   ? {
    //       folderId: rootId,
    //       formId: '',
    //     }
    //   :
    { datasheetId: selectDataSheetProcess?.activeDatasheetId, folderId: rootId };

  return (
    <>
      {children(handelClick)}
      {selectDataSheetProcess && (
        // <DataSourceSelectorForNode
        //   onHide={() => {
        //     selectDataSheetProcess.reject();
        //     setSelectDataSheetProcess(null);
        //   }}
        //   permissionRequired={'manageable'}
        //   onChange={(e) => {
        //     selectDataSheetProcess.resolve(e);
        //     setSelectDataSheetProcess(null);
        //   }}
        //   nodeTypes={[secondConfirmType === SecondConfirmType.AIForm ? ConfigConstant.NodeType.FORM : ConfigConstant.NodeType.DATASHEET]}
        //   defaultNodeIds={defaultNodeIds}
        //   requiredData={secondConfirmType === SecondConfirmType.AIForm ? ['formId'] : ['datasheetId', 'meta']}
        // />
        <div>whatcccc</div>
      )}
    </>
  );
};
