import React from 'react';
import { Switch, Typography, useThemeColors } from '@apitable/components';
import { QuestionCircleOutlined } from '@apitable/icons';
import styles from './style.module.less';

interface IConfigItem {
  controlChildrenVisible?: boolean
  checked?: boolean;
  configTitle: string
  description?: React.ReactNode
  titleSuffix?: React.ReactNode
  docUrl?: string;
  hide?: boolean
  onSwitchChange?(check: boolean): void;
}

export const ConfigItem: React.FC<React.PropsWithChildren<IConfigItem>> = ({
  controlChildrenVisible = false,
  configTitle,
  description,
  checked,
  onSwitchChange,
  children,
  titleSuffix,
  docUrl,
  hide,
}) => {
  const colors = useThemeColors();

  const _onChange = (check: boolean) => {
    onSwitchChange && onSwitchChange(check);
  };

  return <div className={styles.configItem} style={{ display: hide ? 'none' : 'block' }}>
    <div className={styles.title}>
      <div className={styles.left}>
        {
          controlChildrenVisible &&
          <Switch checked={checked} onChange={_onChange} style={{ marginRight: 6 }} size={'small'}/>
        }
        <Typography variant={'h7'} className={styles.configItemTitle}>
          { configTitle }
          { docUrl && <a href={docUrl}><QuestionCircleOutlined/></a> }
        </Typography>
      </div>
      { titleSuffix && <div>{titleSuffix}</div> }
    </div>
    {
      description && <Typography variant={'body4'} color={colors.textCommonTertiary}>
        {description}
      </Typography>
    }
    {
      children && <div style={{ marginTop: 8 }}>
        {children}
      </div>
    }
  </div>;
};
