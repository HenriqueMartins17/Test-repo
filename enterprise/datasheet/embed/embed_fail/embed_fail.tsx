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

import Head from 'next/head';
import Image from 'next/image';
import * as React from 'react';
import { useEffect } from 'react';
import { Strings, t } from '@apitable/core';
import { Logo } from 'pc/components/common';
import { getEnvVariables } from 'pc/utils/env';
import IconFail from 'static/icon/common/common_img_invite_linkfailure.png';
import styles from './style.module.less';

export const EmbedFail: React.FC = () => {
  useEffect(() => {
    window.parent.postMessage({
      message: 'embedLinkFail',
    }, '*');
  }, []);

  const helpUrl = getEnvVariables().EMBED_ERROR_PAGE_HELP_URL;
  return (
    <div className={styles.container}>
      <Head>
        <meta property='og:title' content={t(Strings.unavailable_og_title_content)} />
        <meta property='og:type' content='website' />
        <meta property='og:url' content={window.location.href} />
        <meta property='og:site_name' content={t(Strings.og_site_name_content)} />
        <meta property='og:description' content={t(Strings.embed_fail_og_description_content)} />
      </Head>
      <div className={styles.logo}>
        <Logo size='large' />
      </div>
      <div className={styles.main}>
        <Image src={IconFail} width={480} height={360} alt='' />
        <p className={styles.desc}>{t(Strings.embed_failed)}<a href={helpUrl}>{t(Strings.embed_error_page_help)}</a></p>
      </div>
    </div>
  );
};
