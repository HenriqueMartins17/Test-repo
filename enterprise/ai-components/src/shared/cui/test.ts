import { ICUIForm, Assert } from '../cui/types';

export const form: ICUIForm = {
  // field: 'game',
  // component: 'CUIFormRadio',
  // message: '请选择你喜欢的游戏',
  // props: {
  //   title: '请选择你喜欢的游戏',
  //   options: [
  //     {
  //       label:'王者荣耀',
  //       value:'王者荣耀',
  //       description: '王者荣耀是一款手机MOBA类游戏，腾讯天美工作室群自研打造，不负国民期待，王者荣耀游戏中拥有80余位英雄，近百种特色技能组合，十分钟一局，随时开团的宏大团战，五秒实现超神的爽快操作，缜密的团队策略，和召唤师一起开启无与伦比的手机MOBA新时代。',
  //     },
  //     {
  //       label:'英雄联盟',
  //       value:'英雄联盟',
  //       description: '英雄联盟（League of Legends，简称LOL）是由拳头游戏（Riot Games）开发，腾讯游戏代理运营的英雄对战网游。游戏以《英雄联盟》为背景，拥有数百个个性英雄，并拥有排位系统、符文系统等特色养成系统。',
  //     },
  //     { label:'绝地求生',value:'绝地求生' },
  //   ]
  // },
  field: 'game',
  component: 'CUIFormSelectDatasheet',
  message: '请选择你喜欢的游戏',
  props: {
    title: '请选择你喜欢的游戏',
  },
  next: {
    field: 'name',
    component: 'CUIFormInput',
    message: ['hi 我需要你填写一些信息', '请你如实填写你的名字', '否则我会很生气的'],
    props: {
      title: '请输入你的名字',
    },
    next: [
      {
        condition: '小明',
        assert: Assert.Equal,
        children: {
          field: 'age',
          component: 'CUIFormInput',
          message: '请你如实填写你的年龄',
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
                message: '请选择你喜欢的游戏',
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
                    assert: Assert.Equal,
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
                    assert: Assert.Equal,
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
        assert: Assert.Equal,
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
  },
};
