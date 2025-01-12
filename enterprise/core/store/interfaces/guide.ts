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

import * as actions from '../../../shared/store/action_constants';
import { SystemConfigInterfacePlayer, SystemConfigInterfaceGuide } from '../../../../config/system_config.interface';

// top interfaces
export interface IHooks {
  pendingGuideWizardIds: number[];
  curGuideWizardId: number;
  curGuideStepIds: number[];
  triggeredGuideInfo: ITriggeredGuideInfo;
  config: IWizardsConfig | null;
}

// action
export interface IUpdatePendingGuideWizardIdsAction {
  type: typeof actions.UPDATE_PENDING_GUIDE_WIZARD_IDS;
  payload: number[];
}
export interface IUpdateCurrentGuideWizardIdAction {
  type: typeof actions.UPDATE_CURRENT_GUIDE_WIZARD_ID;
  payload: number;
}
export interface IUpdateCurrentGuideStepIdsAction {
  type: typeof actions.UPDATE_CURRENT_GUIDE_STEP_IDS;
  payload: number[];
}
export interface IUpdateTriggeredGuideInfoAction {
  type: typeof actions.UPDATE_TRIGGERED_GUIDE_INFO;
  payload: ITriggeredGuideInfo;
}
export interface IUpdateConfigAction {
  type: typeof actions.UPDATE_CONFIG;
  payload: IWizardsConfig;
}
export interface IInitHooksDataAction {
  type: typeof actions.INIT_HOOKS_DATA;
}
export interface IClearWizardsDataAction {
  type: typeof actions.CLEAR_WIZARDS_DATA;
}

// data interfaces

type IGuideUiType = 'modal' | 'slideout' | 'hotspot' | 'popover' | 'notice' | 'questionnaire' | 'breath';

export interface IGuideStepInfo {
  ui: IGuideUiType;
  extra: string;
  wizards?: number[];
  backdrop: string;
  next?: string;
  byHook?: string[];
  skip?: string;
  prev?: string;
  nextHook?: string[];
  skipHook?: string[];
  stepId?: number;
  targetHook?: string[];
  mainImg?: string;
}
export interface IWizardsConfig {
  player: SystemConfigInterfacePlayer;
  guide: SystemConfigInterfaceGuide;
}

export interface IGuideWizard {
  wizardId?: number;
  steps: number[];
  hooks: string[];
  rules?: string[];
  module: string;
  repeat?: boolean;
  startTime?: string;
  endTime?: string;
  apiRun: string;
}
export interface IGuideWizardObj { [key: number]: IGuideWizard; }

export interface ITriggeredGuideInfo {
  [key: number]: {
    steps: number[][],
    triggeredSteps: number[][],
  };
}