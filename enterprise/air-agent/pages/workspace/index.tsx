import { createAiAgent, getAiAgentInfo } from 'api/workspace';

import { useGlobalContext } from 'context/global';
import { useRouter } from 'next/router';
import { useEffect } from 'react';

const WorkspacePage = () => {
  const { context } = useGlobalContext();
  const router = useRouter();

  useEffect(() => {
    const redirectToLogin = () => router.push('/login');
    const redirectToWorkspace = (aiId: string) => router.push(`/workspace/${aiId}`);

    const handlePageLoad = async () => {
      if (!context.isLogin) {
        redirectToLogin();
        return;
      }

      try {
        let response: any = await getAiAgentInfo();

        if (response.data.length === 0) {
          response = await createAiAgent({ preAgentId: undefined, name: 'New Agent' });
        }

        const defaultAiID = response.data[0]?.aiId;

        if (defaultAiID) {
          redirectToWorkspace(defaultAiID);
        }
      } catch (error) {
        console.error(error);
      }
    };

    handlePageLoad();
  }, [context.isLogin, router]);

  if (!context.isLogin) {
    return null;
  }

  // return (
  //   <WorkspaceProvider>
  //     <main className={styles.main}>
  //       <WorkspaceLeftSide
  //         className={styles.leftSide}
  //         user={context.user}
  //         aiId={defaultAiID}
  //         theme={context.theme}
  //         setTheme={(theme: ThemeName) => {
  //           context.setTheme(theme);
  //         }}
  //       />
  //       <WorkspaceRightSide className={styles.rightSide} aiId={defaultAiID} />
  //     </main>
  //   </WorkspaceProvider>
  // );

  return null;
};

export default WorkspacePage;
