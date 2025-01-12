import React from 'react';
import { LinkButton, Typography, useThemeColors } from '@apitable/components';
import { Strings, t, ThemeName } from '@apitable/core';
import { Logo } from 'pc/components/common';
import { getEnvVariables } from 'pc/utils/env';
import styles from './style.module.less';

export const Header = (props: { children: any }) => {
  return (
    <header className={styles.header}>
      <div className={styles.contentWidth}>
        {/* FIXME:THEME */}
        <Logo theme={ThemeName.Dark} />
        {props.children}
      </div>
    </header>
  );
};
export const Copyright = () => {
  const colors = useThemeColors();
  return (
    <footer className={styles.copyright}>
      <Typography variant="body3" className={styles.title} color={colors.secondLevelText}>
        {t(Strings.system_configuration_company_copyright)}
      </Typography>
      <LinkButton underline={false} href={getEnvVariables().JOIN_CHATGROUP_PAGE_URL} target="_blank">{t(Strings.contact_us)}</LinkButton>
    </footer>
  );
};
