import React, { useEffect, useState } from 'react';
import { IUserProfile } from 'api/user';
import { WorkspaceContext } from './context';

interface IWorkspaceProvider {
  children: React.ReactNode;
}

export const WorkspaceProvider: React.FC<IWorkspaceProvider> = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<IUserProfile>({} as IUserProfile);

  useEffect(() => {}, []);

  return <WorkspaceContext.Provider value={{ user }}>{children}</WorkspaceContext.Provider>;
};
