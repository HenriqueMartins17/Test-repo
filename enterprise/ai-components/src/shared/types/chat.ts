import { ReactNode } from 'react';
import * as Shared from '@/shared';
import { BaseAction } from '@/shared/cui/action';
import { ICUIForm } from '@/shared/cui/types';
export enum ChatType {
  Bot,
  User,
  System,
  ExploreCard,
  TrainingTips,
  Suggestion,
  Form,
  Divider,
  Banner,
}

export enum ChatStatus {
  WaitResponse,
  Transmission,
  Complete,
  Error,
  Abort,
  Normal,
}

interface IBaseChatItem {
  type: ChatType;
  content?: string | string[] | ReactNode;
  status?: ChatStatus;
  id?: string;
}

export interface ISystemChatItem extends IBaseChatItem {
  type: ChatType.System;
  content: ReactNode;
  layout?: 'left' | 'center';
  onClick?: () => Promise<void>;
}

export interface ITextBubbleItem extends IBaseChatItem {
  type: ChatType.User | ChatType.Bot;
  index?: number;
  /** frontend use update id */
  content?: string;
  feedback?: {
    like: Shared.AIFeedbackType;
    comment: string;
  };
}

interface IExploreCardItem extends IBaseChatItem {
  type: ChatType.ExploreCard;
  content: string[];
  small?: boolean;
}

interface ISuggestionItem extends IBaseChatItem {
  type: ChatType.Suggestion;
  content: string[];
}

interface ITrainingTipsItem extends IBaseChatItem {
  type: ChatType.TrainingTips;
}

interface IDividerItem extends IBaseChatItem {
  type: ChatType.Divider;
  content?: string;
}

interface IBannerItem extends IBaseChatItem {
  type: ChatType.Banner;
  content: string;
  title: string;
  logo: React.ReactNode;
}

export interface ICUIItem extends IBaseChatItem {
  instance: BaseAction;
  type: ChatType.Form;
  result: Record<string, any>;
  form: ICUIForm;
  status: ChatStatus;
}

export type IChatListItem = IBannerItem | ISystemChatItem | ITextBubbleItem | IExploreCardItem | ISuggestionItem | ITrainingTipsItem | ICUIItem | IDividerItem;
