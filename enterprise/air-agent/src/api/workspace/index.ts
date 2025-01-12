import { http } from 'api';

export const getWorkspaceInfo = () => {
  return http.get<IWorkspaceInfo>('/workspace/me');
};

export const getAiAgentInfo = () => {
  return http.get<IAiAgentInfoProps>('/airagent/ai');
};

export const createAiAgent = (data: INewAirAgentInfoProps) => {
  return http.post<IAiAgentInfoProps>('/airagent/ai', data);
};

export const updateAiAgent = (agentId: string, data: IUpdateAiAgentInfoProps) => {
  return http.put<IUpdateAiAgentInfoProps>(`/airagent/ai/${agentId}`, data);
};

export const deleteAiAgent = (agentId: string) => {
  return http.delete(`/airagent/ai/${agentId}`);
};
