import React from 'react';
import { IField } from '@apitable/core';

export enum Assert {
  /* Equals */
  Equal,
  /* Not Equals */
  NotEqual,
  /* Object Comparison */
  EqualObject,
  /* Includes */
  Include,
  /* Does Not Include */
  NotInclude,
  /* Belongs to Collection */
  Collection,
  /* Does Not Belong to Collection */
  NotCollection,
  /* Greater Than */
  GreaterThan,
  /* Less Than */
  LessThan,
  /* Greater Than or Equal To */
  GreaterThanOrEqual,
  /* Less Than or Equal To */
  LessThanOrEqual,
  /* Range */
  Range,
  /* Not in Range */
  NotRange,
}

export interface ICUIChainNext {
  condition?: string | string[] | Record<string, unknown> | unknown;
  assert?: Assert;
  children: ICUIForm;
}

interface IBaseProps {
  title?: string;
  // Description of the component
  description?: string;
  // Default value of the component
  defaultValue?: any;
  // Placeholder text for the component
  placeholder?: string;
  // Flag to show reset button or not
  showReset?: boolean;
  submitText?: string;
}

export interface ICUIFormBase {
  /** field name */
  field: string;
  /**
   * component name
   * 'select' | 'input' | 'checkbox' | 'radio' | 'textarea'
   */
  component: string;
  /** component props */
  props?: IBaseProps;
  /** Assistant message */
  message?: string | string[];
  /**
   * 从上到下的顺序判断匹配一个条件则进入对应的流程
   * 如果 Next 为空 代表流程结束
   */
  next?: ICUIChainNext[] | ICUIForm;
}

export interface IInputProps extends IBaseProps {
  defaultValue?: string;
}

export interface IInputCUIForm extends ICUIFormBase {
  component: 'CUIFormInput';
  props: IInputProps;
}

interface IOptionProps {
  label: string;
  value: string;
  icon?: string;
  desc?: string;
}

export interface ISelectProps extends IBaseProps {
  options: IOptionProps[];
  placeholder?: string;
  defaultValue?: string;
}

export interface ISelectCUIForm extends ICUIFormBase {
  component: 'CUIFormSelect';
  props: ISelectProps;
}

export interface IRadioProps extends IBaseProps {
  options: IOptionProps[];
  defaultValue?: string;
}

export interface IRadioCUIForm extends ICUIFormBase {
  component: 'CUIFormRadio';
  props: IRadioProps;
}

export interface ICUIFormSelectDatasheet extends ICUIFormBase {
  component: 'CUIFormSelectDatasheet';
  props: IBaseProps;
}

export interface IMagicFormProps extends IBaseProps {
  last?: boolean;
  field: IField;
  datasheetId: string;
}

export interface ICUIMagicForm extends ICUIFormBase {
  // component: 'CUIMagicFormText' |
  // 'CUIMagicFormNumber' |
  // 'CUIMagicFormSelect' |
  // 'CUIMagicFormCheckbox' |
  // 'CUIMagicFormDateTime' |
  // 'CUIMagicFormRating' |
  // 'CUIMagicFormEnhanceText' |
  // 'CUIMagicFormAttachment' |
  // 'CUIMagicFormMember' |
  // 'CUIMagicFormCascader' |
  // 'CUIMagicFormLink';
  component: string;
  props: IMagicFormProps;
}

// export type ICUIFormProps = IInputProps | ISelectProps | IRadioProps;
export type ICUIForm = ICUIMagicForm | IInputCUIForm | ISelectCUIForm | IRadioCUIForm | ICUIFormSelectDatasheet;

export enum CUIChatRole {
  System,
  Bot,
  User,
}

export interface ICUIChatProps extends IBaseProps {
  onSubmit: (value: any) => Promise<void>;
  onReset: () => void;
  isComplete?: boolean;
}

export interface ICUIChatBase {
  role: CUIChatRole;
  delay?: number;
}

export interface ICUISystemChat extends ICUIChatBase {
  role: CUIChatRole.System;
  props: ICUIChatProps;
  component: string;
}

export interface ICUIUserChat extends ICUIChatBase {
  role: CUIChatRole.User;
  props: {
    content: string | React.ReactNode;
  };
}
export interface ICUIBotChat extends ICUIChatBase {
  role: CUIChatRole.Bot;
  props: {
    content: string;
  };
}
export type ICUIChat = ICUISystemChat | ICUIUserChat | ICUIBotChat;
