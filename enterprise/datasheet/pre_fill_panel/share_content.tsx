import * as React from 'react';
import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { IconButton, Typography } from '@apitable/components';
import { Api, IFormState, IReduxState, IShareSettings, Selectors, t, Strings } from '@apitable/core';
import { CopyOutlined } from '@apitable/icons';
import { copy2clipBoard } from 'pc/utils';
import { IShareContent } from './interface';
import styles from './style.module.less';

export const ShareContent = (props: IShareContent) => {
  const { suffix } = props;
  const { nodeShared, formId } = useSelector((state: IReduxState) => {
    const formState: IFormState = Selectors.getForm(state)!;
    return {
      nodeShared: formState.nodeShared,
      formId: formState.id,
    };
  });
  const [shareSettings, setShareSettings] = useState<IShareSettings | null>(null);
  const shareHost = `${window.location.protocol}//${window.location.host}/share/`;
  const fetchShareSetting = async (id: string) => {
    const rlt = await Api.getShareSettings(id);
    if (rlt.data.success) {
      setShareSettings(rlt.data.data);
    }
  };
  useEffect(() => {
    fetchShareSetting(formId);
  }, [formId, nodeShared]);
  return nodeShared ? (
    <div className={styles.section}>
      <header>
        <Typography variant="body3">{t(Strings.pre_fill_share_copy_title)}</Typography>
        <IconButton
          shape="square"
          icon={CopyOutlined}
          onClick={() => copy2clipBoard(`${shareHost}${shareSettings?.shareId}${suffix}`)}
        />
      </header>
      <a className={styles.code} href={`${shareHost}${shareSettings?.shareId}${suffix}`} target="_blank" rel="noreferrer">
        {shareHost}{shareSettings?.shareId}{suffix}
      </a>
    </div>
  ) : null;
};
