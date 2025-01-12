import { useRouter } from 'next/router';
import { useGlobalContext } from 'context/global';
import { ChatPage } from 'modules/chat/chat_page';
import styles from './index.module.scss';

const CopilotPage = () => {
  const { context } = useGlobalContext();
  const router = useRouter();

  const secretAIId = typeof router.query.secret === 'string' ? router.query.secret : undefined;

  if (!context.isLogin) {
    router.push('/login');
    return null;
  }

  return (
    <div>
      <main className={styles.main}>{!secretAIId ? null : <ChatPage id={secretAIId} />}</main>
    </div>
  );
};

export default CopilotPage;
