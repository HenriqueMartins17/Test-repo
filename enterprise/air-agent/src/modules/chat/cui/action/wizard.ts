import { ROBOT_TYPE_OPTIONS, MODEL_OPTIONS, ICUIForm, Assert, BaseAction, IAIContextState } from '@apitable/ai';
import * as Shared from '@apitable/ai';
import { t, Strings } from '@apitable/core';
import '../components/select/index';

const form: ICUIForm = {
  field: 'type',
  component: 'CUIFormRadio',
  message: [t(Strings.cui_wizard_welcome_message_1), t(Strings.cui_wizard_welcome_message_2)],
  props: {
    title: t(Strings.cui_wizard_select_chatbot_type),
    options: ROBOT_TYPE_OPTIONS,
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
        },
      },
    },
    {
      assert: Assert.Equal,
      condition: 'chat',
      children: {
        field: 'model',
        message: t(Strings.cui_wizard_select_chatbot_model_message),
        component: 'CUIFormSelect',
        props: {
          defaultValue: 'gpt-3.5-turbo',
          title: t(Strings.cui_wizard_select_chatbot_model),
          options: MODEL_OPTIONS,
        },
      },
    },
  ],
};

export default class WizardAction extends BaseAction {
  public static create(context: IAIContextState) {
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
    const dataSources = values.dataSources || [];

    const id = this.context.data.id;

    await this.context.api.updateAIInfo(
      {
        setting: {
          type: values.type,
        },
      },
      id,
    );

    // datasource
    await this.context.api.addTrainingDataSource(id, dataSources);

    // train
    await this.context.api.train(id);
    await this.context.fetchAIDetail(true, true);
  }
}
