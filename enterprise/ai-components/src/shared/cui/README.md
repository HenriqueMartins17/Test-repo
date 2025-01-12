# CUIForm Design

- 一个CUIForm 从下面这个数据结构开始

## 名词介绍

| 名词          | 描述                                                 |
| ------------- | ---------------------------------------------------- |
| CUIFormXXX    | 一个描述如何渲染组件结构流转Form                     |
| ICUIChainNext | 接下来应该渲染哪个表单，集判断与CUIFormXXX，嵌套模式 |
| Assert        | 断言                                                 |

## Form 基础结构

```ts
export interface ICUIFormBase {
  /**
   * field name
   * 整个form中必须唯一
   */
  field: string;
  /**
   * component name
   * 'CUIFormSelect' | 'CUIFormInput' | 'CUIFormCheckbox' | 'CUIFormRadio' | 'CUIFormTextarea'
   * 组件名可自由定义，上述举例一些基础组件，甚至可以定义成整个form表单 CUIForm?
   * 脑洞大开可以支持 CUIAutomation
   * 新手引导可以是 CUIWizardXXX
   */
  component: string;
  /** Assistant message */
  message?: string | string[];
  /**
   * component props
   * props 是传递给组件的 包含一些基础的props 见 IBaseProps
   */
  props?: IBaseProps;
  /**
   * 从上到下的顺序判断匹配一个条件则进入对应的流程
   * 如果 next 为空 代表流程结束
   * 如果next是 ICUIForm 则继续进入下一个form
   */
  next?: ICUIChainNext[] | ICUIForm;
}
// ICUIForm = ICUISelectForm extends FormBase, input select ...
interface IBaseProps {
  title: string;
  description?: string;
  /** 提交按钮的文字 确认? 下一步? */
  submitText?: string;
  /** 重置按钮的名字 */
  resetText?: string;
  /** 传递给组件的默认值 */
  defaultValue?: any;
}
```

## ICUIChainNext 介绍

- 目前整个 Next Form 由前端计算判断，包含一组断言函数
- 在一个 Next 中 Next Form 是一个数组，从上往下顺序判断，直到进入某一个 children
- 不填写 assert 则代表这是一个兜底的条件，一般他会被放在数组的最后
- 如果均无法匹配，又没有兜底的children，代表流程结束

```ts
export enum Assert {
  /* Equals */
  Equal,
  /* Not Equals */
  NotCompare,
  /* Object Comparison */
  CompareObject,
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
  /** 提交 */
  condition?: string | string[] | Record<string, unknown> | unknown;
  assert?: Assert;
  children: ICUIForm;
}
```

思考：这里的 next 可以是 remote next

## Form收集结构

完成所有next后会生成一份简单的kv对象结构，每个value由组件决定输出什么类型。注意，这里不是嵌套结构。

```json
{
  "name": "小明",
  "game": "王者荣耀",
  "age": 18,
  "form": {
    "field": "content"
    // ...
  }
}
```

## 流程还原

- 前端会根据Form收集结构来随时还原当前表单流程在哪一个步骤
- 注意：如果加入remote next，同时前端还需要保存远程给的form组成一个完整的全新form才能还原

## 限制

- 目前一次只能输出一个组件，暂未做支持多组件的支持，后续可以随时改动逻辑
- 整个 next 目前由前端判断，可以支持 remote next，支持call ai remote

## 现有组件

| 组件名        | 描述         |
| ------------- | ------------ |
| CUIFormInput  | 文本输入组件 |
| CUIFormRadio  | 单选组件     |
| CUIFormSelect | select 组件  |

## 完整的示例

```ts
export const Form: ICUIForm = {
  field: 'name',
  component: 'CUIFormInput',
  props: {
    title: '请输入你的名字',
  },
  next: [
    {
      condition: '小明',
      assert: Assert.Compare,
      children: {
        field: 'age',
        component: 'CUIFormInput',
        props: {
          title: '请输入你的年龄',
        },
        next: [
          {
            condition: 18,
            assert: Assert.GreaterThanOrEqual,
            children: {
              field: 'game',
              component: 'CUIFormRadio',
              props: {
                title: '请选择你喜欢的游戏',
                options: [
                  { label: '王者荣耀', value: '王者荣耀' },
                  { label: '英雄联盟', value: '英雄联盟' },
                  { label: '绝地求生', value: '绝地求生' },
                ],
              },
              next: [
                {
                  condition: '王者荣耀',
                  assert: Assert.Compare,
                  children: {
                    field: 'position',
                    component: 'CUIFormSelect',
                    props: {
                      title: '请选择你擅长的位置',
                      options: [
                        { label: '上单', value: '上单' },
                        { label: '中单', value: '中单' },
                        { label: '打野', value: '打野' },
                        { label: '辅助', value: '辅助' },
                        { label: 'ADC', value: 'ADC' },
                      ],
                    },
                    next: {
                      field: 'rank',
                      component: 'CUIFormSelect',
                      props: {
                        title: '请选择你的段位',
                        options: [
                          { label: '青铜', value: '青铜' },
                          { label: '白银', value: '白银' },
                          { label: '黄金', value: '黄金' },
                          { label: '铂金', value: '铂金' },
                        ],
                      },
                    },
                  },
                },
                {
                  condition: '英雄联盟',
                  assert: Assert.Compare,
                  children: {
                    field: 'role',
                    component: 'CUIFormSelect',
                    props: {
                      title: '请选择你喜欢的角色',
                      options: [
                        { label: '上单', value: '上单' },
                        { label: '中单', value: '中单' },
                        { label: '打野', value: '打野' },
                      ],
                    },
                    next: {
                      field: 'rank',
                      component: 'CUIFormSelect',
                      props: {
                        title: '请选择你的段位',
                        options: [
                          { label: '青铜', value: '青铜' },
                          { label: '白银', value: '白银' },
                          { label: '黄金', value: '黄金' },
                          { label: '铂金', value: '铂金' },
                        ],
                      },
                    },
                  },
                },
              ],
            },
          },
          {
            condition: 18,
            assert: Assert.LessThan,
            children: {
              field: 'cartoon',
              component: 'CUIFormRadio',
              props: {
                title: '请选择你喜欢的动画片',
                options: [
                  { label: '海绵宝宝', value: '海绵宝宝' },
                  { label: '熊出没', value: '熊出没' },
                  { label: '猫和老鼠', value: '猫和老鼠' },
                ],
              },
              next: {
                field: 'color',
                component: 'CUIFormSelect',
                props: {
                  title: '请选择你喜欢的颜色',
                  options: [
                    { label: '红色', value: '红色' },
                    { label: '粉色', value: '粉色' },
                    { label: '紫色', value: '紫色' },
                  ],
                },
                next: {
                  field: 'food',
                  component: 'CUIFormSelect',
                  props: {
                    title: '请选择你喜欢的食物',
                    options: [
                      { label: '火锅', value: '火锅' },
                      { label: '烧烤', value: '烧烤' },
                      { label: '麻辣烫', value: '麻辣烫' },
                    ],
                  },
                },
              },
            },
          },
        ],
      },
    },
    {
      condition: '小红',
      assert: Assert.Compare,
      children: {
        field: 'age',
        component: 'CUIFormInput',
        props: {
          title: '请输入你的年龄',
        },
        next: {
          field: 'color',
          component: 'CUIFormSelect',
          props: {
            title: '请选择你喜欢的颜色',
            options: [
              { label: '红色', value: '红色' },
              { label: '粉色', value: '粉色' },
              { label: '紫色', value: '紫色' },
            ],
          },
        },
      },
    },
  ],
};
```
