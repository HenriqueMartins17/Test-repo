import {
  ROBOT_TYPE_OPTIONS,
  ICUIForm, Assert, BaseAction,
  IAIContextState
} from '@apitable/ai';
import { t, Strings } from '@apitable/core';
import '../components/select/datasheet';
// import { getEnvVariables } from 'pc/utils/env';

export default class WizardAction extends BaseAction {

  public static create(context: IAIContextState) {
    // const env = getEnvVariables();
    // const AI_MODEL = env.IS_APITABLE ? [
    //   'gpt-3.5-turbo',
    //   'gpt-3.5-turbo-0613',
    //   'gpt-3.5-turbo-16k',
    //   'gpt-3.5-turbo-16k-0613',
    //   'gpt-4',
    //   'gpt-4-0613',
    //   // 'gpt-4-32k',
    //   // 'gpt-4-32k-0613',
    // ] : ['ERNIE-Bot-turbo', 'ChatGLM2-6B-32K'];

    // const MODEL_OPTIONS = AI_MODEL.map((item) => ({
    //   value: item,
    //   label: item,
    // }));

    const form: ICUIForm = {
      field: 'type',
      component: 'CUIFormRadio',
      message: [
        t(Strings.cui_wizard_welcome_message_1),
        t(Strings.cui_wizard_welcome_message_2),
      ],
      props: {
        title: t(Strings.cui_wizard_select_chatbot_type),
        options: ROBOT_TYPE_OPTIONS
      },
      next: [
        {
          assert: Assert.Equal,
          condition: 'qa',
          children: {
            field: 'dataSources',
            component: 'CUIFormSelectDatasheet',
            message: t(Strings.cui_wizard_select_datasheet_message),
            props: {
              title: t(Strings.cui_wizard_select_datasheet),
            }
          }
        },
        // {
        //   assert: Assert.Equal,
        //   condition: 'chat',
        //   children: {
        //     field: 'model',
        //     message: t(Strings.cui_wizard_select_chatbot_model_message, { model: AI_MODEL[0] }),
        //     component: 'CUIFormSelect',
        //     props: {
        //       defaultValue: AI_MODEL[0],
        //       title: t(Strings.cui_wizard_select_chatbot_model),
        //       options: MODEL_OPTIONS
        //     }
        //   }
        // }
      ]
    };

    return new WizardAction(context, {
      name: 'WizardAction',
      params: form,
    });
  }

  /**
   * Form data submission after filling
   * @param values
   */
  protected async call(values: Record<string, any>): Promise<void> {
    const dataSources = values.dataSources ? [{ nodeId: values.dataSources.id, viewId: values.dataSources.viewId }] : [];
    const id = this.context.data.id;
    await this.context.api.updateAIInfo({
      type: values.type,
      setting: {
        prologue: values.type === 'chat' ? t(Strings.ai_chat_default_prologue) : t(Strings.ai_default_prologue),
        type: values.type,
        // mode: Shared.IAIMode.Wizard,
        // isEnabledPromptTips: true,
        // isEnabledPromptBox: true,
        // model: values.model ?? 'gpt-3.5-turbo',
      },
      dataSources,
    }, id);
    // train
    await this.context.api.train(id);
    await this.context.fetchAIDetail(true, true);
  }
}