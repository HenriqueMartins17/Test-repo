import { Chat, useAIContext } from '@apitable/ai';
import { Footer } from './footer';
import { WidgetBar } from './widget_bar/widget_bar';
import styles from './style.module.less';

export function ChatPageMain() {
  const hook = useAIContext();
  const { context } = hook;
  const { data: aiInfo } = context;

  return (
    <div className={styles.chatPage}>
      <WidgetBar name={aiInfo.name} />
      <Chat hook={hook} />
      <Footer />
    </div>
  );
}

