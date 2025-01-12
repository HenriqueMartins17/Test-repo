export type AiSetting = QaSetting | ChatSetting;

export type QaSetting = {
  mode: string;
  formId: string;
  isEnabledPromptBox: boolean;
  isEnabledPromptTips: boolean;
};

export type ChatSetting = {
  mode: string;
  isEnabledPromptBox: boolean;
  isEnabledPromptTips: boolean;
};