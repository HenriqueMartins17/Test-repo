import React from 'react';
import { Typography, useResponsive, ScreenSize } from '@apitable/components';
import styles from './index.module.less';

interface IProps {
  children: React.ReactNode;
  title?: string;
  description?: string;
}

export default function CUIFormPanel(props: IProps) {
  const { title, description } = props;
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.sm);

  return (
    <div
      className={styles.panel}
      style={{
        minWidth: isMobile ? '100%' : '450px',
      }}
    >
      {(props.title || props.description) && (
        <div className={styles.header}>
          <Typography variant="h6">{title}</Typography>
          <Typography variant="body2">{description}</Typography>
        </div>
      )}
      {props.children}
    </div>
  );
}
