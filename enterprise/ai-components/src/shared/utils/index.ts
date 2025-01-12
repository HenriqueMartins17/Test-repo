import { Strings, t } from '@apitable/core';
import { AIType } from '@/shared/enum';

export * from './message';
export * from './storage';
export * from './train';

const getSearchParams = () => {
  if (process.env.SSR) {
    return new Map();
  }
  return new URLSearchParams(window.location.search);
};

export const getIsFromIframe = () => {
  return getSearchParams().get('aitable_widget');
};

// ERNIE-Bot-turbo
// ChatGLM2-6B-32K

export const AI_MODEL = [
  'gpt-3.5-turbo',
  'gpt-3.5-turbo-0613',
  'gpt-3.5-turbo-16k',
  'gpt-3.5-turbo-16k-0613',
  'gpt-4',
  'gpt-4-0613',
  // 'gpt-4-32k',
  // 'gpt-4-32k-0613',
];

export const ROBOT_TYPE_OPTIONS = [
  {
    value: AIType.Qa,
    label: 'Q&A',
    desc: t(Strings.cui_wizard_select_chatbot_type_qa_desc),
  },
  {
    value: AIType.Chat,
    label: 'Chat',
    desc: t(Strings.cui_wizard_select_chatbot_type_chat_desc),
  },
];

export const SCORE_THRESHOLD_OPTIONS = [
  {
    label: t(Strings.ai_settings_similarity_filter_relaxed),
    value: 0.0001,
  },
  {
    label: t(Strings.ai_settings_similarity_filter_moderate),
    value: 0.6,
  },
  {
    label: t(Strings.ai_settings_similarity_filter_strict),
    value: 0.8,
  },
];

export const MODEL_OPTIONS = AI_MODEL.map((item) => ({
  value: item,
  label: item,
}));

export const getTypeLabel = (type: AIType) => {
  const item = ROBOT_TYPE_OPTIONS.find((item) => item.value === type);
  return item?.label || '';
};

// export const DEFAULT_QA_SETTINGS: Shared.IAIQASettings = {
//   prologue: t(Strings.ai_default_prologue),
//   prompt: t(Strings.ai_default_prompt),
//   dataSources: [],
//   type: Shared.AIType.Qa,
//   setting: {
//     mode: Shared.IAIMode.Wizard,
//     isEnabledPromptTips: true,
//     isEnabledPromptBox: true,
//     idk: t(Strings.ai_default_idk),
//     isEnableCollectInformation: false,
//     isEnableOpenUrl: false,
//     openUrl: '',
//     openUrlTitle: '',
//     formId: '',
//     temperature: 0.8,
//     scoreThreshold: 0.8,
//   },
//   model: 'gpt-3.5-turbo',
// };

// export const DEFAULT_CHAT_SETTINGS: Shared.IAIChatSettings = {
//   prologue: t(Strings.ai_chat_default_prologue),
//   prompt: t(Strings.ai_chat_default_prompt),
//   type: Shared.AIType.Chat,
//   setting: {
//     mode: Shared.IAIMode.Wizard,
//     isEnabledPromptTips: true,
//     isEnabledPromptBox: true,
//     temperature: 0.8,
//   },
//   model: 'gpt-3.5-turbo',
// };

// const DEFAULT_SETTINGS = {
//   [Shared.AIType.Chat]: DEFAULT_CHAT_SETTINGS,
//   [Shared.AIType.Qa]: DEFAULT_QA_SETTINGS,
// };

// export const getDefaulConfig = (type: Shared.AIType) => {
//   return cloneDeep(DEFAULT_SETTINGS[type]);
// };

// const deepMerge = (target: Shared.IAISettings, source: Shared.IAISettings) => {
//   const loop = (t: any, s: any): any => {
//     if (Array.isArray(t)) {
//       if (!Array.isArray(s)) {
//         return cloneDeep(t);
//       }
//       return s.map((item, index) => loop(item, s[index]));
//     } else if (typeof t === 'object' && t !== null && t !== undefined) {
//       if (typeof s !== 'object' || s === null) {
//         return cloneDeep(t);
//       }
//       const result: any = {};
//       Object.keys(t).forEach((key) => {
//         result[key] = loop(t[key], s[key]);
//       });
//       return result;
//     }
//     if (s !== null && s !== undefined) {
//       return s;
//     }
//     return t;
//   };
//   const result = loop(target, source);
//   return result;
// };

// export const assignConfigs = (data: Shared.IAISettings): Shared.IAISettings => {
//   if (data.type === Shared.AIType.Chat) {
//     return deepMerge(DEFAULT_CHAT_SETTINGS, data);
//   }
//   if (data.type === Shared.AIType.Qa) {
//     return deepMerge(DEFAULT_QA_SETTINGS, data);
//   }
//   return cloneDeep(DEFAULT_CHAT_SETTINGS);
// };
// @ts-ignore
// console.log(deepMerge({ a: 1, b: null }, { a: null, b: { a: 123 }}));
