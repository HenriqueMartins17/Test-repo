
import { ChatPageProvider } from '@apitable/ai';
import { Strings, t } from '@apitable/core';
import { ChatPageMain } from './main';

export default function App() {
  const aiId = 'ai_QZMU1U34rRZ20gS';
  function triggerUsageAlert() {
    alert(t(Strings.subscribe_credit_usage_over_limit));
  }

  return (
    <ChatPageProvider
      key={aiId}
      aiId={aiId}
      childrenNoPermission={<>NoPermission</>}
      triggerUsageAlert={triggerUsageAlert}
      isLogin={false}
    >
      <ChatPageMain />
    </ChatPageProvider>
  );
}
