import { getAiAgentInfo } from 'api/workspace';
import WorkspaceLeftSide from 'components/workspace/left-side';
import WorkspaceRightSide from 'components/workspace/right-side';
import { useGlobalContext } from 'context/global';
import { WorkspaceProvider } from 'context/workspace';
import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import { ThemeName } from '../../../utils/theme';
import styles from '../index.module.scss';

const WorkspaceContentPage = () => {
  const { context } = useGlobalContext();
  const router = useRouter();

  const [userAgentInfoList, setUserAgentInfoList] = useState<IAiAgentInfoProps[]>([]);

  const pathArray = router.query.id as string[];
  const currentAiID = pathArray ? pathArray[0] : undefined;

  const getUserAgentInfo = async () => {
    try {
      const response: any = await getAiAgentInfo();
      setUserAgentInfoList(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    if (context.isLogin) {
      getUserAgentInfo();
    }
  }, [context.isLogin]);

  if (!context.isLogin) {
    router.push('/login');
    return null;
  }

  return (
    <WorkspaceProvider>
      <main className={styles.main}>
        <WorkspaceLeftSide
          className={styles.leftSide}
          user={context.user}
          userAgentInfo={userAgentInfoList}
          refreshUserAgentInfo={getUserAgentInfo}
          aiId={currentAiID}
          theme={context.theme}
          setTheme={(theme: ThemeName) => {
            context.setTheme(theme);
          }}
        />
        <WorkspaceRightSide className={styles.rightSide} aiId={currentAiID} />
      </main>
    </WorkspaceProvider>
  );
};

export default WorkspaceContentPage;
