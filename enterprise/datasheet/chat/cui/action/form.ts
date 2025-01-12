import { BaseAction, IAIContextState, IBaseAction, ICUIForm, registerCUIAction, CUIActionType } from '@apitable/ai';
import { t, Strings, IServerFormPack, FieldType } from '@apitable/core';
import '../components/magic_form';

const FIELD_TYPE_TO_CUI_COMPONENT = {
  [FieldType.Number]: 'CUIMagicFormNumber',
  [FieldType.Currency]: 'CUIMagicFormNumber',
  [FieldType.Percent]: 'CUIMagicFormNumber',
  [FieldType.SingleText]: 'CUIMagicFormText',
  [FieldType.Text]: 'CUIMagicFormText',
  [FieldType.Checkbox]: 'CUIMagicFormCheckbox',
  [FieldType.DateTime]: 'CUIMagicFormDateTime',
  [FieldType.Rating]: 'CUIMagicFormRating',
  [FieldType.URL]: 'CUIMagicFormEnhanceText',
  [FieldType.Email]: 'CUIMagicFormEnhanceText',
  [FieldType.Phone]: 'CUIMagicFormEnhanceText',
  [FieldType.SingleSelect]: 'CUIMagicFormSelect',
  [FieldType.MultiSelect]: 'CUIMagicFormSelect',
  [FieldType.Attachment]: 'CUIMagicFormAttachment',
  [FieldType.Member]: 'CUIMagicFormMember',
  [FieldType.Cascader]: 'CUIMagicFormCascader',
  [FieldType.Link]: 'CUIMagicFormLink',
  [FieldType.OneWayLink]: 'CUIMagicFormLink',
};

const formPackToCUIForm = (data: IServerFormPack): ICUIForm => {
  let form!: ICUIForm;
  let currentForm!: ICUIForm;
  // 遍历 data.snapshot.meta.fieldMap
  const len = data.snapshot.meta.views[0].columns.length;
  for (let i = 0; i < len; i++) {
    const item = data.snapshot.meta.views[0].columns[i];
    const field = data.snapshot.meta.fieldMap[item.fieldId];
    if (!item.hidden) {
      const componentName = FIELD_TYPE_TO_CUI_COMPONENT[field.type];
      if (componentName) {
        const dat: ICUIForm = {
          message: field.desc ? [field.name, field.desc] : field.name,
          component: componentName,
          field: field.id,
          props: {
            // title: field.name,
            field: field,
            datasheetId: data.sourceInfo.datasheetId,
          }
        };
        if (i === len - 1) {
          dat.props.submitText = t(Strings.cui_submit_text);
        }
        if (!form) {
          form = dat;
        } else {
          currentForm.next = dat;
        }
        currentForm = dat;
      } else {
        console.warn(`field type ${field.type} not support CUI Form`);
      }
    }
  }
  return form;
};

export default class MagicFormAction extends BaseAction {
  public static async create(context: IAIContextState) {
    const aiId = context.data.id;
    const ret = await context.api.fetchFormPackForAI(aiId);
    const form = formPackToCUIForm(ret.data);
    return new MagicFormAction(context, {
      name: 'MagicFormAction',
      params: form,
    });
  }
  constructor(context: IAIContextState, data: IBaseAction) {
    super(context, data);
  }

  /**
   * Form data submission after filling
   * @param values
   */
  protected async call(values: Record<string, any>): Promise<void> {
    const aiId = this.context.data.id;
    await this.context.api.addFormRecordForAI(aiId, values);
  }

  // eslint-disable-next-line require-await
  protected override async afterCall(values: Record<string, any>): Promise<void> {
    super.afterCall(values);
    this.context.dispatch({
      type:'insertBotMessage',
      value: t(Strings.ai_form_submit_success),
    });
  }
}

registerCUIAction(CUIActionType.MagicForm, MagicFormAction);
