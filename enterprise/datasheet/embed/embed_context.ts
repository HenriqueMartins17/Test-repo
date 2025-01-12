import { createContext } from 'react';

interface IEmbedContextProps {
  folderId?: string;
}

export const EmbedContext = createContext<IEmbedContextProps | null>(null);
