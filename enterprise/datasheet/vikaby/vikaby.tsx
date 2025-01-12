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

import { useToggle } from 'ahooks';
import { Popover } from 'antd';
import classNames from 'classnames';
import { compact } from 'lodash';
import Image from 'next/image';
import { FC, useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Provider, useSelector } from 'react-redux';
import { useThemeColors, ThemeProvider } from '@apitable/components';
import { ConfigConstant, DATASHEET_ID, Events, isPrivateDeployment, Player, Strings, t, VIKABY_ID, Selectors, ScreenWidth } from '@apitable/core';
import { AdviseOutlined, CommentOutlined, GiftOutlined, PinOutlined, RoadmapOutlined, TimeOutlined } from '@apitable/icons';
import { TriggerCommands } from 'modules/shared/apphook/trigger_commands';
import { ScreenSize } from 'pc/components/common/component_display';
import { AccountCenterModal } from 'pc/components/navigation/account_center_modal';
import { navigationToUrl } from 'pc/components/route_manager/navigation_to_url';
import { useContactUs } from 'pc/hooks/use_contact_us';
import { useResponsive } from 'pc/hooks/use_responsive';
import { store } from 'pc/store';
import { getEnvVariables, isMobileApp } from 'pc/utils/env';
import VikabyActive from 'static/icon/onboarding/organization_img_vikaby_click.png';
import VikabyDefault from 'static/icon/onboarding/organization_img_vikaby_default.png';
import { isWecomFunc } from '../home/social_platform/utils';
import { Dialog, IDialog } from './dialog';
import { TaskCard } from './task_card';
import { TouchMove } from './touch_move';
import styles from './style.module.less';

interface IVikabyBase {
  defaultExpandMenu?: boolean;
  defaultExpandTodo?: boolean;
  defaultExpandDialog?: boolean;
  dialogConfig?: IDialog;
}

interface IOperateVikabyProps extends IVikabyBase {
  visible: boolean;
}

const VIKABY_ID_REMOVER = 'vika-vikaby';
const VIKABY_DEFAULT_POSITION = {
  left: 'calc(100% - 88px)',
  top: 'calc(100% - 88px)',
};
export const VIKABY_POSITION_SESSION_KEY = 'vikaby_position';
export const VIKABY_SUB_POPOVER_CLASS = 'VIKABY_SUB_POPOVER_CONTENT';
export const Vikaby: FC<IVikabyBase> = ({ defaultExpandMenu, defaultExpandTodo, defaultExpandDialog, dialogConfig }) => {
  const colors = useThemeColors();
  const isWecom = isWecomFunc();
  const sessionPosition = sessionStorage.getItem(VIKABY_POSITION_SESSION_KEY);
  const initPosition = sessionPosition ? JSON.parse(sessionPosition) : VIKABY_DEFAULT_POSITION;
  /**
   * Whether the todo card is displayed in document.body or in vikaby's element,
   * the former is currently used as the latter option will trigger a drag event when the todo is clicked
   */

  const [taskCardVisible, { set: setTaskCardVisible }] = useToggle(defaultExpandTodo);
  const [menuVisible, { toggle: toggleMenuVisible, set: setMenuVisible }] = useToggle(defaultExpandMenu);
  const [dialogVisible, { set: setDialogVisible }] = useToggle(defaultExpandDialog);
  const [accountCenterVisible, setAccountCenterVisible] = useState(false);
  const contactUs = useContactUs();
  const { screenIsAtMost } = useResponsive();
  const isMobile = screenIsAtMost(ScreenSize.md);

  const vikabyClick = () => {
    toggleMenuVisible();
    setTaskCardVisible(false);
  };
  const onDragStart = () => {
    console.log(123);
    setMenuVisible(false);
    setTaskCardVisible(false);
    setDialogVisible(false);
  };

  interface IMenuConfigItem {
    icon: JSX.Element;
    title: string;
    onClick: () => void;
    id?: string;
    invalid?: boolean;
  }

  const menuConfig: IMenuConfigItem[] = compact([
    {
      icon: <TimeOutlined color={colors.thirdLevelText} size={16} />,
      title: t(Strings.subscribe_demonstrate),
      onClick: () => navigationToUrl(getEnvVariables().VIKABY_MENU_SUBSCRIBE_DEMONSTRATE_FORM_URL),
      invalid: isMobile || !getEnvVariables().VIKABY_MENU_SUBSCRIBE_DEMONSTRATE_FORM_URL,
    },
    {
      icon: <CommentOutlined color={colors.thirdLevelText} />,
      title: t(Strings.player_contact_us),
      onClick: () => {
        contactUs();
        setMenuVisible(false);
      },
      invalid: !window.location.pathname.includes('workbench'),
    },
    !isWecom && {
      icon: <RoadmapOutlined color={colors.thirdLevelText} />,
      title: t(Strings.assistant_release_history),
      id: VIKABY_ID.UPDATE_LOGS_HISTORY,
      onClick: () => {
        const url = getEnvVariables().ASSISTANT_RELEASE_CHANGELOGS_PAGE_URL;
        navigationToUrl(url);
        setMenuVisible(false);
      },
    },
    !isWecom && {
      icon: <GiftOutlined color={colors.thirdLevelText} />,
      title: t(Strings.assistant_beginner_task),
      onClick: () => {
        setTaskCardVisible(true);
        setMenuVisible(false);
      },
    },
    !isWecom && {
      icon: <AdviseOutlined color={colors.thirdLevelText} />,
      title: t(Strings.user_feedback),
      onClick: () => {
        navigationToUrl(getEnvVariables().USER_FEEDBACK_FORM_URL);
        setMenuVisible(false);
      },
    },
    {
      icon: <PinOutlined color={colors.thirdLevelText} />,
      title: t(Strings.assistant_hide),
      onClick: () => {
        setMenuVisible(false);
        Player.doTrigger(Events.workbench_hidden_vikaby_btn_clicked);
        localStorage.setItem('vikaby_closed', 'true');
        destroyVikaby();
      },
    },
  ]);
  const handleMenuVisibleChange = (visible?: boolean) => {
    setMenuVisible(visible);
  };

  const vikaby = (taskCardVisible || menuVisible || dialogVisible) ? VikabyActive : VikabyDefault;

  useEffect(() => {
    if (!menuVisible) {
      const state = store.getState();
      const hooks = state.hooks;
      const { curGuideWizardId } = hooks;
      if (curGuideWizardId === ConfigConstant.WizardIdConstant.VIKABY_UPDATE_LOGS_HISTORY) {
        TriggerCommands.clear_guide_all_ui?.();
      }
    }
  }, [menuVisible]);

  const popoverVisible = useMemo(() => {
    return taskCardVisible || dialogVisible;
  }, [taskCardVisible, dialogVisible]);

  const outerPopoverContent = useMemo(() => {
    if (taskCardVisible) {
      return {
        overlayClassName: classNames(VIKABY_SUB_POPOVER_CLASS, styles.vikabyTaskCard),
        content: (
          <TaskCard onClose={() => setTaskCardVisible(false)} setAccountCenterVisible={setAccountCenterVisible} />
        ),
      };
    } else if (dialogVisible) {
      return {
        overlayClassName: classNames(
          VIKABY_SUB_POPOVER_CLASS,
          styles.vikabyDialog,
          dialogConfig?.dialogClx === 'billingNotify' ? styles.billingNotify : dialogConfig?.dialogClx,
        ),
        content: (
          <Dialog
            {...dialogConfig}
            onClose={() => {
              dialogConfig?.onClose && dialogConfig.onClose();
              setDialogVisible(false);
            }}
          />
        ),
      };
    }
    return {};
  }, [taskCardVisible, dialogVisible, setDialogVisible, setTaskCardVisible, dialogConfig]);

  if (isMobileApp() || isPrivateDeployment()) {
    return null;
  }

  return (
    <>
      <Popover
        {...outerPopoverContent}
        placement='leftBottom'
        open={popoverVisible}
        destroyTooltipOnHide
      >
        <Popover
          content={<>{menuConfig.map(item =>
            (!item.invalid &&
              <div
                onClick={item.onClick}
                className={styles.vikabyMenuItem}
                key={item.title}
                id={item.id}
              >
                {item.icon}{item.title}
              </div>
            ),
          )}</>}
          trigger='click'
          overlayClassName={styles.vikabyMenu}
          placement='topRight'
          open={menuVisible}
          onOpenChange={handleMenuVisibleChange}
        >
          <TouchMove
            id={VIKABY_ID_REMOVER}
            sessionStorageKey={VIKABY_POSITION_SESSION_KEY}
            initPosition={initPosition}
            onClick={vikabyClick}
            onDragStart={onDragStart}
          >
            <div className={styles.vikabyWrap} id={DATASHEET_ID.VIKABY}>
              <div className={styles.vikaby} style={{ position: 'relative' }}>
                <Image src={vikaby} layout={'fill'} draggable={false} />
              </div>
            </div>
          </TouchMove>
        </Popover>
      </Popover>
      {accountCenterVisible && <AccountCenterModal setShowAccountCenter={setAccountCenterVisible} />}
    </>
  );
};

const VikabyWithTheme: FC<IVikabyBase> = (props) => {
  const cacheTheme = useSelector(Selectors.getTheme);
  return (
    <ThemeProvider theme={cacheTheme}>
      <Vikaby {...props} />
    </ThemeProvider>
  );
};

export const showVikaby = (props?: IVikabyBase) => {
  if (getEnvVariables().IS_SELFHOST || getEnvVariables().IS_APITABLE) {
    return;
  }
  const render = () => {
    setTimeout(() => {
      const dom = document.querySelector(`#${VIKABY_ID_REMOVER}`);
      if (dom) {
        return;
      }
      const div = document.createElement('div');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        (<Provider store={store}>
          <VikabyWithTheme {...props} />
        </Provider>));
    });
  };

  const run = () => {
    destroyVikaby();
    render();
  };

  const state = store.getState();
  const templateId = state.pageParams.templateId;
  const shareId = state.pageParams.shareId;
  const isPc = window.innerWidth > ScreenWidth.md;
  if (!templateId && !shareId && window.location.pathname.includes('workbench') && isPc) {
    run();
  }
};

export const destroyVikaby = () => {
  const destroy = () => {
    const dom = document.querySelector(`#${VIKABY_ID_REMOVER}`);
    if (dom && dom.parentNode) {
      document.body.removeChild(dom.parentNode);
    }

    const subPopover = document.querySelector(`.${VIKABY_SUB_POPOVER_CLASS}`);
    if (subPopover && subPopover.parentNode && subPopover.parentNode.parentNode) {
      document.body.removeChild(subPopover.parentNode.parentNode);
    }
  };
  destroy();
};

export const openVikaby: (data: IOperateVikabyProps) => void = (props) => {
  const { visible, ...rest } = props;
  if (visible) {
    localStorage.removeItem('vikaby_closed');
    showVikaby({ ...rest });
  } else {
    destroyVikaby();
  }
};
