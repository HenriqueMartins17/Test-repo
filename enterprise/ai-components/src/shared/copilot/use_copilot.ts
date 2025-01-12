import { useContext, useMemo } from 'react';
import { CopilotContext, CopilotContextState } from '@/shared/copilot';

export interface ICopilotContextHook {
  context: CopilotContextState;
}

export const useCopilotContext = (): ICopilotContextHook => {
  const context = useContext(CopilotContext);

  // Here you can encapsulate some commonly used methods and expose them to components for use.
  return {
    context,
  };
};
