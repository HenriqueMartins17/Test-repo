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
import { IWizardsConfig, ITriggeredGuideInfo } from '../../../../exports/store/interfaces';

export function updatePendingGuideWizardsIds(guideWizardIds: number[]) {
  return {
    type: actions.UPDATE_PENDING_GUIDE_WIZARD_IDS,
    payload: guideWizardIds,
  };
}

export function updateCurrentGuideStepIds(steps: number[]) {
  return {
    type: actions.UPDATE_CURRENT_GUIDE_STEP_IDS,
    payload: steps,
  };
}
export function updateCurrentGuideWizardId(id: number) {
  return {
    type: actions.UPDATE_CURRENT_GUIDE_WIZARD_ID,
    payload: id,
  };
}

export function updateTriggeredGuideInfo(triggeredGuideInfo: ITriggeredGuideInfo) {
  return {
    type: actions.UPDATE_TRIGGERED_GUIDE_INFO,
    payload: triggeredGuideInfo,
  };
}
export function updateConfig(config: IWizardsConfig) {
  return {
    type: actions.UPDATE_CONFIG,
    payload: config,
  };
}
export function initHooksData() {
  return {
    type: actions.INIT_HOOKS_DATA,
  };
}

export function clearWizardsData() {
  return {
    type: actions.CLEAR_WIZARDS_DATA,
  };
}