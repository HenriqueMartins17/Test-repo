import classNames from 'classnames';
import { useSelector } from 'react-redux';
import { ChatPageProvider } from '@apitable/ai';
import { Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { ScreenSize } from 'pc/components/common/component_display';
import { Logo } from 'pc/components/common/logo';
import { TComponent } from 'pc/components/common/t_component';
import { NoPermission } from 'pc/components/no_permission';
import { useResponsive } from 'pc/hooks';
import { getReleaseVersion } from 'pc/utils/env';
import { ChatWelcomeMain } from './main';
import { triggerUsageAlertUniversal } from 'enterprise/billing';
import style from './index.module.less';

export const ChatWelcome = () => {
  const colors = useThemeColors();
  function triggerUsageAlert() {
    triggerUsageAlertUniversal(t(Strings.subscribe_credit_usage_over_limit));
  }
  const user = useSelector((state) => state.user);
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);
  const version = getReleaseVersion();

  return (
    <div className={classNames(style.welcome, { [style.mobile]: isMobile })}>
      <div className={style.welcomeHeader}>
        <Logo size={40} text={false} />
        <Typography variant={'h3'} className={'!vk-text-center'}>
          {t(Strings.welcome_chat_bot_title)}
        </Typography>
        <Typography variant={'body2'} className={'!vk-text-center !vk-mb-6'}>
          <TComponent tkey={t(Strings.welcome_chat_bot_desc)} params={{
            helpCenter: <a rel="noreferrer" href="https://help.aitable.ai" target={'_blank'}
              style={{ color: colors.textLinkDefault }}>{t(Strings.help_center)}</a>,
          }} />
        </Typography>
      </div>
      <ChatPageProvider
        key="ai_ZkmsmAX0kbok895"
        // aiId="ai_lCjRkoKqobz4xZB"
        aiId={(version === 'development' || version.includes('alpha')) ? 'ai_jHH0lfo1eRjfYrt' : 'ai_ZkmsmAX0kbok895'}
        // aiId='ai_i4gYCHlSo2eTxuK' // vika
        childrenNoPermission={<NoPermission />}
        triggerUsageAlert={triggerUsageAlert}
        isLogin={user.isLogin}
      >
        <ChatWelcomeMain />
      </ChatPageProvider>

    </div>
  );
};
