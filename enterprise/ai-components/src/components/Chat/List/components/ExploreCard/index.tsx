import classNames from 'classnames';
import { useState } from 'react';
import { Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { RefreshOutlined } from '@apitable/icons';
import styles from './style.module.less';
import { Loading } from '@/components';
import { ScreenSize } from '@/shared';
import { useResponsive } from '@/shared/hook/use_responsive';

interface IExploreCardProps {
  content: string[];
  small?: boolean;
  onClick?: (item: string) => void;
  refresh?: () => Promise<void>;
}

export function MessageExploreCard(props: IExploreCardProps) {
  const { content } = props;
  const [loading, setLoading] = useState(false);
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  const color = useThemeColors();

  const refresh = async () => {
    if (props.refresh) {
      try {
        setLoading(true);
        await props.refresh();
      } finally {
        setLoading(false);
      }
    }
  };

  if (!content) return null;

  return (
    <div className={styles.exploreCardWrapper}>
      <div className={classNames(styles.colorBlock, styles.red)} />
      <div className={classNames(styles.colorBlock, styles.blue)} />
      <div className={styles.head}>
        <Typography style={{ flex: 1 }} variant={isMobile || props.small ? 'h8' : 'h6'}>
          <span dangerouslySetInnerHTML={{ __html: t(Strings.ai_explore_card_title) }} />
        </Typography>
        {props.refresh && (
          <div className={styles.refreshBtn} onClick={refresh}>
            <RefreshOutlined color={color.primaryColor} />
            {t(Strings.ai_explore_refresh_btn_text)}
          </div>
        )}
      </div>
      <div style={{ paddingLeft: 16 }} className={styles.box}>
        {content.map((item, index) => {
          return (
            <Typography
              variant={isMobile || props.small ? 'body3' : 'body2'}
              key={index}
              style={{ cursor: 'pointer' }}
              onClick={() => {
                if (props.onClick) {
                  props.onClick(item);
                }
              }}
              className={styles.questionItem}
            >
              {item}
            </Typography>
          );
        })}
      </div>
      {loading && <Loading className={styles.loading} />}
    </div>
  );
}
