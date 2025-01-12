import { ChatPageProvider } from '@apitable/ai';
import { Strings, t } from '@apitable/core';
import { usageWarnModal } from 'components/usage_warn_modal/usage_warn_modal';
import { useGlobalContext } from 'context/global';
import { ChatPageMain } from './main';

interface IProps {
  id: string;
  isShareMode?: boolean;
}

export const ChatPage = (props: IProps) => {
  const { id, isShareMode } = props;

  const { context } = useGlobalContext();

  function triggerUsageAlert() {
    usageWarnModal({
      alertContent: t(Strings.subscribe_credit_usage_over_limit),
      email: context.user?.email ?? '',
    });
  }

  return (
    <ChatPageProvider
      key={id}
      aiId={id ?? ''}
      childrenNoPermission={<div>no permission</div>}
      triggerUsageAlert={triggerUsageAlert}
      isLogin={context.isLogin}
      baseURL="/api/v1/airagent"
    >
      <ChatPageMain isShareMode={isShareMode} />
    </ChatPageProvider>
  );
};
