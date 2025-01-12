import { ChatPage } from 'modules/chat/chat_page';
import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import { http } from '@apitable/core';
import styles from './index.module.scss';

const ShareAgentPage = () => {
  const router = useRouter();
  const [aiId, setAiId] = useState<string | undefined>();
  const { id } = router.query as { id: string };
  const getShareInfo = async () => {
    const res = await http.get(`/api/v1/airagent/ai/share/${id}`, { baseURL: '' });
    setAiId(res.data.ai.id);
  };
  useEffect(() => {
    getShareInfo();
  }, [id]);

  return (
    aiId && <ChatPage id={aiId} isShareMode />
  );
};

export default ShareAgentPage;
