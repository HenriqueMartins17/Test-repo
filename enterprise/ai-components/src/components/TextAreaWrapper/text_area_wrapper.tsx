import React from 'react';
import { useResponsive, ScreenSize, Tooltip, IconButton, useThemeColors } from '@apitable/components';
import styles from './style.module.less';

export interface ITextAreaWrapperProps {
  children: React.ReactNode;
  leftToolbar?: React.ReactNode[];
  rightToolbar?: React.ReactNode[];
}
export function TextAreaWrapper(props: ITextAreaWrapperProps) {
  const { children, leftToolbar = [], rightToolbar = [] } = props;
  const colors = useThemeColors();
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  return (
    <div className={styles.footer}>
      <div className={styles.footerTop}>
        <div className={styles.footerTopLeftToolbar}>
          {leftToolbar.map((item, index) => (
            <div className={styles.footerTopLeftToolbarItem} key={index}>
              {item}
            </div>
          ))}
        </div>
        <div className={styles.footerTopRightToolbar}>
          {rightToolbar.map((item, index) => (
            <div className={styles.footerTopRightToolbarItem} key={index}>
              { item }
            </div>
          ))}
        </div>
      </div>
      {children}
    </div>
  );
}
