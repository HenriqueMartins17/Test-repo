interface IAiAgentInfoProps {
  aiId: string;
  agentName: string;
  agentId: string;
  preAgentId?: string;
  icon?: string;
}

interface IWorkspaceInfo {}

interface INewAirAgentInfoProps {
  preAgentId?: string;
  name: string;
}

interface IUpdateAiAgentInfoProps {
  name?: string;
  type?: string;
  model?: string;
  setting?: object;
}
