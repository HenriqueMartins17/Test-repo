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

import { batchActions } from 'redux-batched-actions';
import { StoreActions, ConfigConstant, IReduxState } from '@apitable/core';
import { isTimeRulePassed } from 'modules/shared/player/rules';
import { store } from 'pc/store';
import { isMobileApp, getEnvVariables } from 'pc/utils/env';
import { Guide } from './guide';
import {
  getPrevAndNextStepIdsInCurWizard,
  getPrevAndNextIdInArr,
  getWizardInfo,
  addWizardNumberAndApiRun,
} from './utils';

interface IOpenGuideNextStepProps {
  clearAllPrevUi?: boolean;
}

interface ISkipCurrentWizardProps {
  curWizardCompleted?: boolean;
}

interface ISetWizardCompletedProps {
  curWizard?: boolean;
  wizardId?: number;
}

const checkIfWizardsNeedDisabled = (state: IReduxState, wizardId?: number) => {
  if (
    wizardId &&
    (wizardId === ConfigConstant.WizardIdConstant.REPLAY_GANTT_VIDEO ||
      wizardId === ConfigConstant.WizardIdConstant.REPLAY_CALENDAR_VIDEO ||
      wizardId === ConfigConstant.WizardIdConstant.REPLAY_ORG_CHART_VIDEO)
  ) {
    return false;
  }

  const { embedId } = state.pageParams;

  return Boolean(embedId) || getEnvVariables().IS_SELFHOST;
};

export const openGuideWizard = (wizardId: number, ignoreRepeat?: boolean) => {
  const state = store.getState();
  const hooks = state.hooks;
  const config = hooks.config;
  const user = state.user;

  if (checkIfWizardsNeedDisabled(state, wizardId)) return;
  if (!config || !user.info) return;

  const curWizard = getWizardInfo?.(config, wizardId);

  if (!curWizard || !isTimeRulePassed(curWizard.startTime, curWizard.endTime))
    return;
  if (!ignoreRepeat && !curWizard.repeat && user.info.wizards.hasOwnProperty(wizardId)) return;

  store.dispatch(StoreActions.updateCurrentGuideWizardId(wizardId));
};

export const openGuideWizards = (wizards: number[]) => {
  const state = store.getState();
  const hooks = state.hooks;
  const config = hooks.config;
  const user = state.user;
  if (checkIfWizardsNeedDisabled(state)) return;
  if (!config) return;
  const pendingWizardIds: number[] = [];
  // Filter out unqualified wizards
  wizards.forEach((id) => {
    const curWizard = getWizardInfo?.(config, id);
    if (!curWizard || !isTimeRulePassed(curWizard.startTime, curWizard.endTime))
      return;
    if (localStorage.getItem(`${id}`)) return;
    if (
      isMobileApp() &&
      id === ConfigConstant.WizardIdConstant.AGREE_TERMS_OF_SERVICE
    )
      return;
    if (curWizard?.repeat || !user.info?.wizards.hasOwnProperty(id)) {
      pendingWizardIds.push(id);
    }
  });
  if (pendingWizardIds.length === 0) return;
  store.dispatch(
    batchActions([
      StoreActions.updateCurrentGuideWizardId(pendingWizardIds[0]),
      StoreActions.updatePendingGuideWizardsIds(pendingWizardIds),
    ])
  );
};

export const openGuideNextStep = (
  commandMap: any,
  props?: IOpenGuideNextStepProps
) => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  if (props?.clearAllPrevUi) {
    commandMap.clear_guide_all_ui();
  }
  const hooks = state.hooks;
  const { curGuideWizardId, config, curGuideStepIds, pendingGuideWizardIds } =
    hooks;
  if (!config || !getPrevAndNextStepIdsInCurWizard) return;
  const nextStepIds = getPrevAndNextStepIdsInCurWizard(
    config,
    curGuideWizardId,
    curGuideStepIds
  )[2];
  const nextWizardId = getPrevAndNextIdInArr(
    pendingGuideWizardIds,
    curGuideWizardId
  )[2];
  if (nextStepIds.length === 0 && nextWizardId !== -1) {
    store.dispatch(StoreActions.updateCurrentGuideWizardId(nextWizardId));
    return;
  }
  store.dispatch(StoreActions.updateCurrentGuideStepIds(nextStepIds));
};

export const skipCurrentWizard = (props?: ISkipCurrentWizardProps) => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  if (!getPrevAndNextIdInArr) return;
  const hooks = state.hooks;
  const { curGuideWizardId, config, pendingGuideWizardIds } = hooks;
  if (props?.curWizardCompleted) {
    addWizardNumberAndApiRun(curGuideWizardId);
  }
  const nextWizardId = getPrevAndNextIdInArr(
    pendingGuideWizardIds,
    curGuideWizardId
  )[2];
  if (nextWizardId === -1 || !config) {
    store.dispatch(StoreActions.updateCurrentGuideStepIds([]));
    return;
  }
  store.dispatch(StoreActions.updateCurrentGuideWizardId(nextWizardId));
};

export const skipAllWizards = () => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  store.dispatch(StoreActions.updateCurrentGuideStepIds([]));
};

export const clearGuideUis = (arr: string[]) => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  if (arr.length > 0 && Guide) {
    arr.forEach((item) => {
      Guide.destroyUi(item);
    });
  }
};

export const clearGuideAllUi = (commandMap: any) => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  commandMap.clear_guide_uis([
    'notice',
    'modal',
    'questionnaire',
    'popover',
    'breath',
    'slideout',
    'taskList',
    'contactUs',
  ]);
};

export const setWizardCompleted = (props: ISetWizardCompletedProps) => {
  const state = store.getState();
  if (checkIfWizardsNeedDisabled(state)) return;
  if (!addWizardNumberAndApiRun) return;
  const { curWizard, wizardId } = props;
  if (curWizard) {
    const hooks = state.hooks;
    const { curGuideWizardId } = hooks;
    return addWizardNumberAndApiRun(curGuideWizardId);
  }
  if (typeof wizardId === 'number' && wizardId > -1) {
    return addWizardNumberAndApiRun(wizardId);
  }
  return Promise.reject('wizardId is not a number');
};
