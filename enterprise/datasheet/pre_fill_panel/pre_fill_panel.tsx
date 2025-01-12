import { Tabs } from 'antd';
import * as React from 'react';
import { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import { IconButton, Typography, useThemeColors } from '@apitable/components';
import { t, Strings, StoreActions } from '@apitable/core';
import { BookOutlined, ChevronRightOutlined, CopyOutlined, CloseOutlined } from '@apitable/icons';
// eslint-disable-next-line no-restricted-imports
import { Message, Tooltip } from 'pc/components/common';
import { copy2clipBoard } from 'pc/utils';
import { IPreFillPanel } from './interface';
import { ShareContent } from './share_content';
import { formData2String } from './util';
import styles from './style.module.less';

const { TabPane } = Tabs;

export const PreFillPanel = (props: IPreFillPanel) => {
  const { formData, fieldMap, setPreFill, columns } = props;
  const visibleColumns = columns.filter(column => !column.hidden);
  const colors = useThemeColors();
  const dispatch = useDispatch();
  const [suffix, setSuffix] = useState('');

  const copySuccess = () => {
    Message.success({ content: t(Strings.copy_success) });
  };

  useEffect(() => {
    const urlStrings = formData2String(formData, fieldMap, visibleColumns);
    setSuffix(urlStrings);
  }, [fieldMap, formData, visibleColumns]);

  useEffect(() => {
    if (window.innerWidth < 1500) {
      dispatch(StoreActions.setSideBarVisible(false));
    }
  }, [dispatch]);

  return (
    <div className={styles.preFillPanel}>
      <header>
        <Tabs activeKey="pre_fill" hideAdd>
          <TabPane key="pre_fill" tab={<Typography variant="h6">{t(Strings.pre_fill_title)}</Typography>} />
          <TabPane disabled key="copilot" tab={
            <div className={styles.copilotTab}>
              <Typography variant="h6">{t(Strings.ai_assistant)}</Typography>
              <div className={styles.comingSoon}>{t(Strings.coming_soon)}</div>
            </div>
          } />
        </Tabs>
        <IconButton
          shape="square"
          onClick={() => setPreFill(false)}
          icon={CloseOutlined}
        />
      </header>
      <div className={styles.content}>
        <a className={styles.guideWrap} href={t(Strings.pre_fill_help)} target="_blank" rel="noreferrer">
          <span className={styles.left}>
            <BookOutlined size={16} color={colors.primaryColor}/>
            <Typography variant="body3" color={colors.secondLevelText}>
              {t(Strings.pre_fill_helper_title)}
            </Typography>
          </span>
          <ChevronRightOutlined size={16} color={colors.thirdLevelText}/>
        </a>
        <Typography variant="body4" className={styles.tips}>
          {t(Strings.pre_fill_content)}
        </Typography>
        <div className={styles.section}>
          <header>
            <Typography variant="body3">{t(Strings.pre_fill_copy_title)}</Typography>
            <Tooltip title={t(Strings.copy_link)}>
              <IconButton
                shape="square"
                icon={CopyOutlined}
                onClick={() => copy2clipBoard(`${window.location.origin + window.location.pathname}${suffix}`, copySuccess)}
              />
            </Tooltip>
          </header>
          <a className={styles.code} href={window.location.origin + window.location.pathname + suffix} target="_blank" rel="noreferrer">
            {window.location.origin + window.location.pathname}{suffix}
          </a>
        </div>
        <ShareContent suffix={suffix}/>
      </div>
    </div>
  );
};
