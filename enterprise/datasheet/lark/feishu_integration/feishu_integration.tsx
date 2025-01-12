import * as React from 'react';
import { PropsWithChildren, useState } from 'react';
import { Loading } from '@apitable/components';
import { Api, IUserInfo } from '@apitable/core';
import { useRequest } from 'pc/hooks';
import { Copyright } from '../../dingtalk';
import { FeishuIntegrationHeader } from './feishu_integration_header';
import FeishuSyncConcat from './feishu_sync_concat';
import styles from './styles.module.less';

const FeishuIntegration: React.FC<PropsWithChildren<any>> = ({ children }) => {
  const [userInfo, setUserInfo] = useState<IUserInfo | null>(null);
  const { loading: isLoginStatusGetting } = useRequest(() => Api.getUserMe().then(res => {
    const { data, success } = res.data;
    if (success) {
      setUserInfo(data);
    }
  }));

  if (isLoginStatusGetting || !userInfo) {
    return <Loading />;
  }

  return (
    <div className={styles.feishuIntegrationWrap}>
      <div className={styles.feishuIntegration}>
        <FeishuIntegrationHeader userInfo={userInfo} />
        <div className={styles.container}>
          {children ? children : <FeishuSyncConcat spaceId={userInfo.spaceId} />}
          {/*<Routes>*/}
          {/*  <Route*/}
          {/*    path="sync/:appInstanceId"*/}
          {/*    element={<FeishuSyncConcat spaceId={userInfo.spaceId} />}*/}
          {/*  />*/}
          {/*  <Route path="config/:appInstanceId" element={<FeishuConfig />} />*/}
          {/*  <Route path="bind/:appInstanceId" element={<FeishuIntegrationBind />} />*/}
          {/*</Routes>*/}
        </div>
        <Copyright />
      </div>
    </div>
  );
};

export default FeishuIntegration;
