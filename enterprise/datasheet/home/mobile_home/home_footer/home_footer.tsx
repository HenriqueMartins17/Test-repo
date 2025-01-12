/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { FC } from 'react';
import { ButtonGroup, LinkButton } from '@apitable/components';
import { isPrivateDeployment, Navigation, Strings, t } from '@apitable/core';
import { Router } from 'pc/components/route_manager/router';
import { getEnvVariables, isMobileApp } from 'pc/utils/env';

import styles from './style.module.less';

export const HomeFooter: FC = () => {
  const { LOGIN_OFFICIAL_WEBSITE_URL } = getEnvVariables();
  const env = getEnvVariables();

  if (isMobileApp()) {
    return (
      <ButtonGroup className={styles.homeFooter} withSeparate>
        <LinkButton
          component='button'
          underline={false}
          onClick={() => Router.push(Navigation.TEMPLATE)}
        >
          {t(Strings.nav_templates)}
        </LinkButton>
      </ButtonGroup>
    );
  }

  return (
    <>
      <ButtonGroup className={styles.homeFooter} withSeparate>
        {!isPrivateDeployment() &&
          <LinkButton
            component='button'
            underline={false}
          >
            <a href={`${window.location.origin}/chatgroup/`} target='_blank' style={{ color: 'inherit' }} rel='noreferrer'>{t(Strings.feedback)}</a>
          </LinkButton>}
        <LinkButton
          component='button'
          underline={false}
        >
          <a href='/help/' target='_blank' style={{ color: 'inherit' }}>{t(Strings.support)}</a>
        </LinkButton>
        <LinkButton
          component='button'
          underline={false}
          onClick={() => Router.push(Navigation.TEMPLATE)}
        >
          {t(Strings.nav_templates)}
        </LinkButton>
      </ButtonGroup>
      <ButtonGroup className={styles.homeFooter} withSeparate>
        <LinkButton
          component='button'
          underline={false}
          onClick={() => {
            if (LOGIN_OFFICIAL_WEBSITE_URL) {
              window.open(LOGIN_OFFICIAL_WEBSITE_URL, '__blank');
              return;
            }
            Router.newTab(Navigation.HOME, { query: { home: 1 } });
          }}>{t(Strings.enter_official_website)}
        </LinkButton>
        {
          env.PRIVATE_DEVELOPMENT_FORM && <LinkButton
            component='button'
            underline={false}
          >
            <a href={env.PRIVATE_DEVELOPMENT_FORM} target='_blank' style={{ color: 'inherit' }} rel='noreferrer'>{t(Strings.self_hosting)}</a>
          </LinkButton>
        }
      </ButtonGroup>
    </>
  );
};
