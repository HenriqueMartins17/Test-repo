import { Strings, t } from '@apitable/core';
// import { getReleaseVersion } from 'pc/utils/env';
import { ASSERT_FUNCTION } from '../assert';
import { ICUIChainNext, ICUIForm } from '../types';
import { IAIContextState } from '@/shared/types';
import { ChatType } from '@/shared/types/chat';

export enum CUIActionType {
  MagicForm = 'MagicForm',
  Wizard = 'Wizard',
}

interface ICUIActions {
  [key: string]: typeof BaseAction;
}

export const CUIActions: ICUIActions = {};

export const registerCUIAction = (name: CUIActionType, action: typeof BaseAction) => {
  CUIActions[name] = action;
};

export interface IBaseAction {
  name: string;
  description?: string;
  params: ICUIForm;
}

export abstract class BaseAction {
  public id: string;
  protected name: string;
  protected description?: string;
  protected params: ICUIForm;
  protected context: IAIContextState;

  constructor(context: IAIContextState, data: IBaseAction) {
    this.name = data.name;
    this.description = data.description;
    this.params = data.params;
    this.context = context;
    this.id = Date.now().toString() + Math.random();
  }

  protected abstract call(values: Record<string, any>): Promise<void>;

  // eslint-disable-next-line require-await
  protected async afterCall(values: Record<string, any>): Promise<void> {
    // const version = getReleaseVersion();
    //     if (version === 'development' || version.includes('alpha')) {
    //       this.context.dispatch({
    //         type: 'insertBotMessage',
    //         value: `已经完成并提交了数据 此消息只有 development/integration 可见
    // \`\`\`json
    // ${JSON.stringify(values, null, 2)}
    // \`\`\``,
    //       });
    //     }
  }

  public start() {
    this.context.dispatch({ type: 'startCUIAction', value: { form: this.params, result: {}, instance: this } });
  }

  public update(values: Record<string, any>): void {
    this.context.dispatch({ type: 'updateCUIAction', value: { form: this.params, result: values, id: this.id } });
  }

  public exit(): void {
    this.context.dispatch({ type: 'finishCUIAction', value: { id: this.id } });
    this.context.dispatch({
      type: 'insertMessageList',
      value: {
        data: [{ type: ChatType.Divider, content: t(Strings.cui_chat_exit_message) }],
        append: true,
      },
    });
  }

  public async finish(values: Record<string, any>): Promise<void> {
    try {
      await this.call(values);
    } catch (e: any) {
      return;
    }
    this.update(values);
    this.context.dispatch({ type: 'finishCUIAction', value: { id: this.id } });
    await this.afterCall(values);
  }

  public hasNextForm = (item: ICUIForm, value: any) => {
    let nextForm!: ICUIForm;
    const next = item.next;
    if (next) {
      if (Array.isArray(next) && next.length) {
        const form = this.getNextForm(next, value);
        if (form) {
          nextForm = form;
        }
      } else {
        nextForm = next as ICUIForm;
      }
    }
    return nextForm;
  };

  public getNextForm = (next: ICUIChainNext[], value: any): ICUIForm | void => {
    for (let i = 0; i < next.length; i++) {
      const item = next[i];
      if (item) {
        const assert = item.assert;
        if (item.condition && assert !== undefined) {
          const fn = ASSERT_FUNCTION[assert];
          if (fn) {
            if (fn(value as never, item.condition as never)) {
              return item.children;
            }
          }
        } else {
          return item.children;
        }
      }
    }
  };
}
