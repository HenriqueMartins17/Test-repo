import React, { useContext } from 'react';
import { ThemeName } from '@apitable/components';
import { IUserProfile } from 'api/user';

export interface IGlobalContextState {
  readonly user: IUserProfile;
  readonly isLogin: boolean;
  readonly isInit: boolean;
  readonly theme?: ThemeName;
  readonly setTheme: (theme: ThemeName) => void;
  readonly fetchUserInfo: () => Promise<void>;
}

export const GlobalContext = React.createContext<IGlobalContextState>({} as IGlobalContextState);

export const useGlobalContext = () => {
  const context = useContext(GlobalContext);
  return {
    context,
  };
};
