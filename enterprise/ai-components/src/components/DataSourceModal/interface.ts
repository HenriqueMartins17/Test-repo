import React from 'react';
import { TabKey } from '@/components/DataSourceModal/const';
import { IAISourceDatasheet, IDataSourceUpdate } from '@/shared';

export interface IDataSourceModalProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  tabConfig?: {
    [k in keyof typeof TabKey]: null | React.ReactElement;
  } & {
    [key: string]: React.ReactElement;
  };
  setDataSource: React.Dispatch<React.SetStateAction<IAISourceDatasheet[]>>;
  onChange?: (data: IDataSourceUpdate) => Promise<void>;
}

export type ITabCommonProps = Pick<IDataSourceModalProps, 'setDataSource' | 'setOpen'>;
