import React, { useContext } from 'react';
import { IUserProfile } from 'api/user';

export interface IWorkspaceContextState {
  readonly user: IUserProfile;
}

export const WorkspaceContext = React.createContext<IWorkspaceContextState>({} as IWorkspaceContextState);

export const useWorkspaceContext = () => {
  const context = useContext(WorkspaceContext);
  return {
    context,
  };
};
