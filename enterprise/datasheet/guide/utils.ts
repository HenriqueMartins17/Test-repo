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

import { difference } from 'lodash';
import { batchActions } from 'redux-batched-actions';
import { IWizardsConfig, Api, StoreActions, IUser, Selectors } from '@apitable/core';
import { Step } from '@apitable/core/src/config/system_config.interface';
import { TriggerCommands, startActions } from 'modules/shared/apphook/trigger_commands';
import { Message } from 'pc/components/common/message/message';
import { store } from 'pc/store';
import { Guide } from '../guide';

export const isEqualArr = (arr1: number[], arr2: number[]) => {
  const differenceArr = difference(arr1, arr2);
  return differenceArr.length ===0 && arr1.length === arr2.length;
};

export const getPrevAndNextIdInArr = (arr: any[], curId: number | number[]) => {
  const isArray = typeof curId === 'object';
  const initValue = isArray ? [] : -1;

  const curInx = arr.findIndex(item => {
    return isArray ? isEqualArr(item, curId as number[]) : item === curId;
  });
  if(curInx === -1) return [initValue, curId, initValue];

  const prevId = arr[curInx - 1] || initValue;
  const nextId = curInx === arr.length -1 ? initValue : arr[curInx + 1];
  return [prevId, curId, nextId];
};

export const getPrevAndNextStepIdsInCurWizard = (config: IWizardsConfig, curWizardId: number, curStepIds: number[]) => {
  const curWizardInfo = getWizardInfo(config, curWizardId);
  if(!curWizardInfo) return [[], curStepIds, []];
  return getPrevAndNextIdInArr([...curWizardInfo.steps], curStepIds);
};

export const getWizardInfo = (config: IWizardsConfig, wizardId: number) => {
  const wizardInfoConfig = config.guide.wizard[wizardId];
  if(wizardInfoConfig && wizardInfoConfig.hasOwnProperty('steps')){
    const stepsConfig = wizardInfoConfig.steps;
    const steps = typeof stepsConfig === 'string' ? JSON.parse(stepsConfig || '[]') : stepsConfig;
    return {
      ...wizardInfoConfig,
      steps,
    };
  }
  return { ...wizardInfoConfig };
};
export const getWizardFreeVCount = (config: IWizardsConfig, wizardId: number) => {
  const wizardInfo = getWizardInfo(config, wizardId);
  return wizardInfo.freeVCount || 0;
};
export const addWizardNumberAndApiRun = (wizardId: number) => {
  const state = store.getState();
  const hooks = state.hooks;
  const user = state.user;
  return Api.triggerWizard(wizardId).then(res=>{
    if(res.data.success){
      store.dispatch(StoreActions.addWizardNumber(wizardId));
      if(!hooks.config || !user.info) return;
    }else{
      Message.error({ content: res.data.message });
    }
  });
};

export const getWizardRunCount = (user: IUser, wizardId: number) => {
  const curWizards = user.info ? { ...user.info.wizards } : {};
  if (!curWizards.hasOwnProperty(wizardId)) return 0;
  return curWizards[wizardId];
};

export const clearWizardsData = () => {
  store.dispatch(StoreActions.clearWizardsData());
};

let curSteps: number[] = [];

export function currentStepInHook() {
  const previousCurrentStep = curSteps;
  const state = store.getState();
  const { curGuideStepIds, config, triggeredGuideInfo, curGuideWizardId } = state.hooks;
  curSteps = curGuideStepIds;
  if (isEqualArr(curSteps, previousCurrentStep) || !config) {
    return;
  }

  // If step is an empty array, then all bootstrapping is complete and all hook data is cleared
  if (curSteps.length === 0) {
    TriggerCommands.clear_guide_all_ui?.();
    store.dispatch(StoreActions.updateCurrentGuideWizardId(-1));
    store.dispatch(StoreActions.updatePendingGuideWizardsIds([]));
    store.dispatch(StoreActions.updateTriggeredGuideInfo({}));
    return;
  }
  const curWizardInfo = getWizardInfo?.(config, curGuideWizardId as number);
  const { steps, completeIndex } = curWizardInfo;
  // Rendering pages
  setTimeout(()=>{
    curSteps.forEach(item => {
      const curStepInfo = config && config.guide && config.guide.step[item];
      if(!curStepInfo) return;
      const objectKeys = Object.keys(curStepInfo) || [];
      if(objectKeys.length === 0) {
        // If there are empty objects in the configuration table, end the guide to avoid errors on the page
        store.dispatch(StoreActions.updateCurrentGuideStepIds([]));
        return;
      }
      Guide.showUiFromConfig(curStepInfo as Step);
    });
  }, 500);

  // Put this step into the completed wizards array
  const curTriggerWizardInfo = curGuideWizardId in triggeredGuideInfo ?
    {
      ...triggeredGuideInfo[curGuideWizardId],
      triggeredSteps: [...triggeredGuideInfo[curGuideWizardId].triggeredSteps, curSteps],
    } :
    {
      steps,
      triggeredSteps: [curSteps],
    };
  const newTriggeredGuideInfo = {
    ...triggeredGuideInfo,
    [curGuideWizardId]:curTriggerWizardInfo,
  };
  store.dispatch(StoreActions.updateTriggeredGuideInfo(newTriggeredGuideInfo));

  // Send request

  const curIndex = steps.findIndex((item: number[]) => isEqualArr(item, curSteps));
  if(completeIndex !== -1 && curIndex === completeIndex){
    // This is deliberately triggered as a macro task, in order to avoid two triggers occurring at the same time
    setTimeout(()=>{
      addWizardNumberAndApiRun?.(curGuideWizardId);
    }, 100);
  }
}

let curId = -1;

export function currentGuideWizardIdInHook() {
  const previousCurWizardId = curId;
  const state = store.getState();
  const { curGuideWizardId, config, triggeredGuideInfo } = state.hooks;
  const user = state.user;
  curId = curGuideWizardId;

  if (curId === previousCurWizardId || !config || !user || !user.info) return;
  const nextWizardInfo = getWizardInfo(config, curId as number);
  if (!nextWizardInfo) return;

  if (nextWizardInfo.manualActions) {
    startActions(config, nextWizardInfo.manualActions);
  }

  if (nextWizardInfo.steps && nextWizardInfo.steps.length > 0) {

    const newTriggeredGuideInfo = {
      ...triggeredGuideInfo,
      [curId]: {
        steps: nextWizardInfo.steps,
        triggeredSteps: [],
      },
    };

    store.dispatch(batchActions([
      StoreActions.updateTriggeredGuideInfo(newTriggeredGuideInfo),
      StoreActions.updateCurrentGuideStepIds(nextWizardInfo.steps[0]),
    ]));
  }

}

export const updatePlayerConfig = (config: IWizardsConfig) => {
  store.dispatch(StoreActions.updateConfig(config));
};

export const getPlayerHooks = () => {
  return Selectors.hooksSelector(store.getState());
};

