import React from 'react';
import { IAIContextState } from '@/shared/types';

export const AIContext = React.createContext<IAIContextState>({} as IAIContextState);
