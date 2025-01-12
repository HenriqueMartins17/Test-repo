import React, { useEffect, useState } from 'react';
import { Skeleton, ThemeProvider } from '@apitable/components';
import { StatusCode } from '@apitable/core';
import { IUserProfile, getUserInfo } from 'api/user';
import { GlobalContext } from './context';
import useTheme from './theme';

interface IGlobalProvider {
  children: React.ReactNode;
}

export const GlobalProvider: React.FC<IGlobalProvider> = ({ children }) => {
  const [isInit, setIsInit] = useState(true);
  const [isLogin, setIsLogin] = useState(false);
  const [user, setUser] = useState<IUserProfile>({} as IUserProfile);
  const [initFailMessage, setInitFailMessage] = useState('');
  const { theme, setTheme } = useTheme();

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo();
      setUser(res.data);
      setIsLogin(true);
    } catch (e) {
      if (e.code === StatusCode.UN_AUTHORIZED) {
        setIsLogin(false);
      } else {
        throw e;
      }
    }
  };

  useEffect(() => {
    setIsInit(true);
    fetchUserInfo()
      .then(() => {
        setIsInit(false);
      })
      .catch((e) => {
        setIsInit(false);
        setInitFailMessage(e.message);
      });
  }, []);

  if (isInit) {
    /**
     * 这里换成 loading 或者空白
     * 第一次无论如何都要获取用户信息后才能开始渲染
     * 否则每个页面都要处理用户逻辑
     */
    return <Skeleton />;
  }

  return (
    <GlobalContext.Provider
      value={{
        isInit,
        user,
        isLogin,
        theme,
        setTheme,
        fetchUserInfo,
      }}
    >
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </GlobalContext.Provider>
  );
};
