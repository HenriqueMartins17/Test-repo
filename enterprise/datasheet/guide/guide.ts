/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

/*
 * Service for bootstrapping systems
 *
 * @Author: Kelly Peilin Chan (kelly@vikadata.com)
 * @Date: 2020-03-19 12:40:19
 * @Last Modified by: skyhuang
 * @Last Modified time: 2022-12-16 16:29:08
 */
import parser from 'html-react-parser';
import { isMobile } from 'react-device-detect';
import { Api, ConfigConstant, ScreenWidth, Strings, t } from '@apitable/core';
import { Step as IStep } from '@apitable/core/src/config/system_config.interface';
import { startActions, TriggerCommands } from 'modules/shared/apphook/trigger_commands';
import { store } from 'pc/store';
import { getInitializationData, getEnvVariables } from 'pc/utils/env';

import { destroyBillingStrip, IGuideBillingStripProps, showBillingStrip } from './ui/billing_strip';
import { destroyBreath, IGuideBreathOptions, showBreath } from './ui/breath';
import { destroyContactUs, IGuideContactUsOptions, showContactUs } from './ui/contact_us';
import { destroyCustomTemplate, IGuideCustomTemplateOptions, showCustomTemplate } from './ui/custom_template';
import { destroyModal, IGuideModalOptions, showModal } from './ui/modal';
import { destroyNotice, IGuideNoticeOptions, showNotice } from './ui/notice/notice';
import { IShowPopoverOptions, showPopover } from './ui/popover';
import { destroyPrivacyModal, IPrivacyModalProps, showPrivacyModal } from './ui/privacy_modal';
import { destroyQuestionnaire, IQuestionnaireProps, showQuestionnaire } from './ui/questionnaire';
import { showSlideout, destroySlideout, IGuideSlideOutProps } from './ui/slideout';
import { destroyTaskList, IGuideTaskListProps, showTaskList } from './ui/task_list';
import { addWizardNumberAndApiRun } from './utils';

/**
 * Newbie Guide Service Class
 *
 * @export
 * @class Guide
 */
export class Guide {
  static showUiFromConfig(stepInfo: IStep): void {
    const state = store.getState();
    const hooks = state.hooks;
    const user = state.user;
    const {
      uiType,
      uiConfig,
      uiConfigId,
      prev,
      nextId,
      next,
      skipId,
      skip,
      onClose: closeActions,
      onTarget: targetActions,
      onSkip: skipActions,
      onNext: nextActions,
      onPlay: playActions,
      onPrev: prevActions,
      backdrop,
    } = stepInfo;
    const _uiConfig = uiConfigId && Strings[uiConfigId] && uiType !== 'notice' ? t(Strings[uiConfigId]) : uiConfig;
    const _next = nextId && Strings[nextId] ? t(Strings[nextId]) : next;
    const _skip = skipId && Strings[skipId] ? t(Strings[skipId]) : skip;   
    try {
      const uiInfo = JSON.parse(_uiConfig);
      const onSkip = () => {
        hooks.config && skipActions && startActions(hooks.config, skipActions);
      };
      const onNext = () => {
        hooks.config && nextActions && startActions(hooks.config, nextActions);
      };
      const onPrev = () => {
        hooks.config && prevActions && startActions(hooks.config, prevActions);
      };
      const onClose = () => {
        hooks.config && closeActions && startActions(hooks.config, closeActions);
      };
      const onTarget = () => {
        hooks.config && targetActions && startActions(hooks.config, targetActions);
      };
      const onPlay = () => {
        hooks.config && playActions && startActions(hooks.config, playActions);
      };
      const buttonConfig = { prev, next: _next, skip: _skip, onPrev, onNext, onSkip };

      switch (uiType as any) {
        case 'notice': {
          this.showNotice({
            ...uiInfo,
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            readMoreTxt: _next,
            onClose() {
              onClose();
              // Depending on whether the wizard of the "History Update Button" on the vikaby menu has been accessed
              // If not, and you are not mobile, open the vikaby menu and execute the corresponding wizard.
              const curWizards = user.info ? { ...user.info.wizards } : {};
              const isPc = window.innerWidth > ScreenWidth.md;
              const VIKABY_UPDATE_LOGS_HISTORY = ConfigConstant.WizardIdConstant.VIKABY_UPDATE_LOGS_HISTORY;
              if (!curWizards.hasOwnProperty(VIKABY_UPDATE_LOGS_HISTORY) && isPc) {
                TriggerCommands.open_vikaby?.({ defaultExpandMenu: true, visible: true });
                TriggerCommands.open_guide_wizard?.(ConfigConstant.WizardIdConstant.VIKABY_UPDATE_LOGS_HISTORY);
              }
            },
          });
          break;
        }
        case 'privacyModal': {
          this.showPrivacyModal({
            ...uiInfo,
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            onClose,
          });
          break;
        }
        case 'modal': {
          this.showModal({
            ...uiInfo,
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            onClose,
            onPlay,
            autoPlay: uiInfo.autoPlay,
          });
          break;
        }
        case 'customTemplate': {
          this.showCustomTemplate({
            ...uiInfo,
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            onClose,
          });
          break;
        }
        case 'popover': {
          this.showPopover({
            ...uiInfo,
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            buttonConfig,
            onTarget,
            backdrop,
          });
          break;
        }
        case 'breath': {
          this.showBreath({
            ...uiInfo,
            onTarget,
            backdrop,
          });
          break;
        }
        case 'slideout': {
          this.showSlideout({
            ...uiInfo,
            buttonConfig,
          });
          break;
        }
        case 'questionnaire': {
          // Newcomer guide pop-ups - Questionnaire
          this.showQuestionnaire({
            ...uiInfo,
            onClose: () => {
              onClose();
              addWizardNumberAndApiRun(hooks.curGuideWizardId);
            },
            children: uiInfo.children ? parser(uiInfo.children) : undefined,
            onSubmit: (answers) => {
              localStorage.setItem('vika_guide_start', 'questionnaire');
              addWizardNumberAndApiRun(hooks.curGuideWizardId);
              const env = getInitializationData().env;
              const submitData = {
                userId: user.info?.uuid,
                nickName: user.info?.nickName,
                env,
                ...answers,
              };

              // TODO：Here the Message is hidden pending subsequent interaction optimisation
              Api.submitQuestionnaire(submitData);
              // .then(res => {
              //   const { msg } = res.data;
              //   Message.success({ content: msg === 'success' ? t(Strings.submit_questionnaire_success) : msg });
              // });
            },
            onNext,
          });
          break;
        }
        case 'customQuestionnaire': {
          this.showCustomQuestionnaire('e0cb5f411286a4c0');
          break;
        }
        case 'afterSignNPS': {
          this.showCustomQuestionnaire('ff8d129b2e37c77c');
          break;
        }
        case 'taskList': {
          this.showTaskList({
            ...uiInfo,
          });
          break;
        }
        case 'contactUs': {
          this.showContactUs({
            uiInfo,
            onClose,
            confirmText: _next,
          });
          break;
        }
        case 'billingStrip': {
          this.showBillingStrip({
            uiConfig: uiInfo,
            confirmText: _next,
            skipText: _skip,
            onClose,
          });
          break;
        }
      }
    } catch (_error) {
      return;
    }
  }

  static destroyUi(uiType: string) {
    switch (uiType) {
      case 'notice': {
        destroyNotice();
        break;
      }
      case 'privacyModal': {
        destroyPrivacyModal();
        break;
      }
      case 'modal': {
        destroyModal();
        break;
      }
      case 'questionnaire': {
        destroyQuestionnaire();
        break;
      }
      case 'popover': {
        showPopover({ hidden: true });
        break;
      }
      case 'breath': {
        destroyBreath();
        break;
      }
      case 'slideout': {
        destroySlideout();
        break;
      }
      case 'taskList': {
        destroyTaskList();
        break;
      }
      case 'contactUs': {
        destroyContactUs();
        break;
      }
      case 'billingStrip': {
        destroyBillingStrip();
        break;
      }
      case 'customTemplate': {
        destroyCustomTemplate();
        break;
      }
    }
  }

  static showQuestionnaire(props: IQuestionnaireProps) {
    showQuestionnaire(props);
  }

  static showTaskList(props: IGuideTaskListProps) {
    showTaskList(props);
  }

  static showSlideout(props: IGuideSlideOutProps) {
    showSlideout(props);
  }

  static showNotice(props: IGuideNoticeOptions) {
    showNotice(props);
  }

  static showPrivacyModal(props: IPrivacyModalProps) {
    showPrivacyModal(props);
  }

  static showPopover(props: IShowPopoverOptions) {
    showPopover(props);
  }

  static showModal(props: IGuideModalOptions) {
    showModal(props);
  }

  static showCustomTemplate(props: IGuideCustomTemplateOptions) {
    showCustomTemplate(props);
  }

  static showBreath(props: IGuideBreathOptions) {
    showBreath(props);
  }

  /* nps Documentation of requirements：https://vikadata.feishu.cn/docs/doccnWmDDh29H89CdKMSYOiHRaf */
  static showCustomQuestionnaire(npsId: string) {
    if (isMobile || getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE) {
      // No questionnaires are displayed on mobile
      return;
    }
    (function (a, b, c, d) {
      a['npsmeter'] =
        a['npsmeter'] ||
        function () {
          (a['npsmeter'].q = a['npsmeter'].q || []).push(arguments);
        };
      a['_npsSettings'] = { npsid: npsId, npssv: '1.01' };
      const e = b.getElementsByTagName('head')[0];
      const f = b.createElement('script');
      f.async = 1 as any as boolean;
      f.src = c + d + a['_npsSettings'].npssv + '&npsid=' + a['_npsSettings'].npsid;
      e.appendChild(f);
    })(window, document, 'https://static.npsmeter.cn/npsmeter', '.js?sv=');

    const initData = getInitializationData();
    window['npsmeter']({
      key: npsId,
      user_id: initData.userInfo?.userId, // Replace with user id
      user_name: initData.userInfo?.nickName, // Replace with username
    });

    setTimeout(() => {
      window['npsmeter']?.open();
    }, 5000);
  }

  static showContactUs(props: IGuideContactUsOptions) {
    showContactUs(props);
  }

  static showBillingStrip(props: IGuideBillingStripProps) {
    showBillingStrip(props);
  }
}
