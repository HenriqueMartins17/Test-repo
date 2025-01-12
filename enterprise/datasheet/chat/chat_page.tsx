
import { useSelector } from 'react-redux';
import { ChatPageProvider } from '@apitable/ai';
import { Strings, t } from '@apitable/core';
import { NoPermission } from 'pc/components/no_permission';
import { ChatPageMain } from './main';
import { triggerUsageAlertUniversal } from 'enterprise/billing';

export const ChatPage = () => {
  const { aiId } = useSelector(state => state.pageParams);
  const user = useSelector((state) => state.user);

  function triggerUsageAlert() {
    triggerUsageAlertUniversal(t(Strings.subscribe_credit_usage_over_limit));
  }

  return (
    <ChatPageProvider
      key={aiId as string}
      aiId={aiId ?? ''}
      childrenNoPermission={<NoPermission />}
      triggerUsageAlert={triggerUsageAlert}
      isLogin={user.isLogin}
    >
      <ChatPageMain />
    </ChatPageProvider>
  );
};
