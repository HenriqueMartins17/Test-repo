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

import { produce } from 'immer';
import {
  IClearWizardsDataAction, IHooks, IInitHooksDataAction, IUpdateConfigAction, IUpdateCurrentGuideStepIdsAction, IUpdateCurrentGuideWizardIdAction,
  IUpdatePendingGuideWizardIdsAction, IUpdateTriggeredGuideInfoAction,
} from '../../../../exports/store/interfaces';
import * as actions from '../../../shared/store/action_constants';

type IHooksActions = IUpdatePendingGuideWizardIdsAction | IUpdateCurrentGuideStepIdsAction |
  IUpdateConfigAction | IUpdateTriggeredGuideInfoAction | IInitHooksDataAction | IUpdateCurrentGuideWizardIdAction |
  IClearWizardsDataAction;
declare const window: any;
const config = (
  typeof window === 'object' &&
  (window as any).__initialization_data__ &&
  (window as any).__initialization_data__.wizards
) || null;

const initState = {
  pendingGuideWizardIds: [],
  curGuideStepIds: [],
  curGuideWizardId: -1,
  triggeredGuideInfo: {},
};
const defaultState: IHooks = {
  ...initState,
  config,
};
export const guide = produce((data: IHooks = defaultState, action: IHooksActions) => {
  switch (action.type) {
    case actions.UPDATE_PENDING_GUIDE_WIZARD_IDS: {
      data.pendingGuideWizardIds = action.payload;
      return data;
    }
    case actions.UPDATE_CURRENT_GUIDE_STEP_IDS: {
      data.curGuideStepIds = action.payload;
      return data;
    }
    case actions.UPDATE_TRIGGERED_GUIDE_INFO: {
      data.triggeredGuideInfo = action.payload;
      return data;
    }
    case actions.UPDATE_CURRENT_GUIDE_WIZARD_ID: {
      data.curGuideWizardId = action.payload;
      return data;
    }
    case actions.UPDATE_CONFIG: {
      data.config = action.payload;
      return data;
    }
    case actions.INIT_HOOKS_DATA: {
      data = {
        ...defaultState,
        config: data.config,
      };
      return data;
    }
    case actions.CLEAR_WIZARDS_DATA: {
      data = {
        ...initState,
        config: data.config,
      };
      return data;
    }
    default:
      return data;
  }
}, defaultState);
