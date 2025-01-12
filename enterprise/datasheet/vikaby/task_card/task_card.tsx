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
import classNames from 'classnames';
import Image from 'next/image';
import * as React from 'react';
import { FC, useCallback, useMemo } from 'react';
import { createRoot } from 'react-dom/client';
import { useDispatch, useSelector, Provider } from 'react-redux';
import { useThemeColors } from '@apitable/components';
import { ConfigConstant, IReduxState, StoreActions, Strings, t } from '@apitable/core';
import { CloseOutlined, ChevronRightOutlined, EmailOutlined, BulbOutlined } from '@apitable/icons';
import { TriggerCommands } from 'modules/shared/apphook/trigger_commands';
import { Message } from 'pc/components/common/message/message';
import { store } from 'pc/store';
import Bg from 'static/icon/onboarding/organization_img_bj.png';
import Video1 from 'static/icon/onboarding/video1.png';
import Video2 from 'static/icon/onboarding/video2.png';
import Video3 from 'static/icon/onboarding/video3.png';
import Video4 from 'static/icon/onboarding/video4.png';
import GoldImg from 'static/icon/workbench/workbench_account_gold_icon.png';
import { ScrollBar } from '../../../../pc/components/scroll_bar';
import { addWizardNumberAndApiRun, getWizardFreeVCount } from '../../guide/utils';
import { getSocialWecomUnitName } from '../../home/social_platform/utils';
import { Progress } from '../progress';
import styles from './style.module.less';

export const GUIDE_TASK_CARD_ID = 'GUIDE_TASK_CARD_ID';

interface ITaskCard {
  style?: React.CSSProperties;
  onClose?: () => void;
  setAccountCenterVisible?: React.Dispatch<React.SetStateAction<boolean>>;
}

const SCROLL_LIMIT_NUM = 115;
export const TaskCard: FC<ITaskCard> = (props) => {
  const colors = useThemeColors();

  const { onClose, setAccountCenterVisible } = props;
  const dispatch = useDispatch();
  const user = useSelector((state: IReduxState) => state.user.info);
  const spaceInfo = useSelector(state => state.space.curSpaceInfo);
  const hooksConfig = useSelector((state: IReduxState) => state.hooks.config);
  const [isInTop, { set: setIsInTop }] = useToggle(true);

  const title = getSocialWecomUnitName({
    name: user?.memberName,
    isModified: user?.isMemberNameModified,
    spaceInfo,
  });

  const getTodoConfig = useMemo(() => {
    let maxCount = 0;
    let curCount = 0;
    const todoConfigBase = [
      {
        wizardId: 14,
        title: t(Strings.assistant_beginner_task_1_what_is_datasheet),
        leftIcon: <Image src={Video1} width={80} height={44} alt="" />,
      },
      {
        wizardId: 15,
        title: t(Strings.assistant_beginner_task_2_quick_start),
        leftIcon: <Image src={Video2} width={80} height={44} alt="" />,
      },
      {
        wizardId: 16,
        title: t(Strings.assistant_beginner_task_3_how_to_use_datasheet),
        leftIcon: <Image src={Video3} width={80} height={44} alt="" />,
      },
      {
        wizardId: 17,
        title: t(Strings.assistant_beginner_task_4_share_and_invite),
        leftIcon: <Image src={Video4} width={80} height={44} alt="" />,
      },
      {
        wizardId: 18,
        title: t(Strings.assistant_beginner_task_5_onboarding),
        leftIcon: <span className={styles.iconWrap} style={{ background: colors.rc07 }}><BulbOutlined /></span>,
        rightIcon: <ChevronRightOutlined size={45} />,
      },
      {
        wizardId: ConfigConstant.WizardIdConstant.EMAIL_BIND,
        title: t(Strings.assistant_beginner_task_6_bind_email),
        leftIcon: <span className={styles.iconWrap} style={{ background: colors.rc02 }}><EmailOutlined /></span>,
        rightIcon: <ChevronRightOutlined size={45} />,
      },
    ];
    const stepCount = todoConfigBase.length;
    let doneStepCount = 0;
    const todoConfig = todoConfigBase.map(item => {
      const onClick = () => {
        if (item.wizardId === ConfigConstant.WizardIdConstant.EMAIL_BIND) {
          setAccountCenterVisible && setAccountCenterVisible(true);
          if (user!.email) {
            Message.info({ content: t(Strings.email_bound) });
            !(item.wizardId in user!.wizards) && addWizardNumberAndApiRun(item.wizardId);
          }
        } else {
          dispatch(StoreActions.clearWizardsData());
          TriggerCommands.open_guide_wizard?.(item.wizardId);
        }
        // destroyTodoList();
        onClose && onClose();
      };
      const vCount = hooksConfig && getWizardFreeVCount(hooksConfig!, item.wizardId) || 0;
      const isDone = Boolean(user && item.wizardId in user.wizards);

      maxCount += vCount;
      if (isDone) {
        curCount += vCount;
        doneStepCount += 1;
      }
      return {
        ...item,
        onClick,
        isDone,
        vCount,
      };
    });

    return { todoConfig, maxCount, curCount, stepCount, doneStepCount };
  }, [hooksConfig, user, dispatch, onClose, setAccountCenterVisible, colors]);
  const { todoConfig, maxCount, curCount, stepCount, doneStepCount } = getTodoConfig;

  const onScroll = useCallback((e: any) => {
    if (typeof e.target.scrollTop !== 'number') return;
    setIsInTop(e.target.scrollTop < SCROLL_LIMIT_NUM);
  }, [setIsInTop]);

  const renderScrollContent = useMemo(() => {
    return (
      <ScrollBar onScroll={onScroll}>
        <span className={styles.img}>
          <Image src={Bg} alt="" />
        </span>
        <div className={styles.todoTop}>
          <div className={styles.title}>HI,{title}</div>
          <div className={styles.subTitle}>{t(Strings.assistant_beginner_task_title1)}</div>
          <div className={styles.desc}>{t(Strings.assistant_beginner_task_title2)}</div>
        </div>
        <div className={styles.progressWrap}>
          <div className={styles.progressTitle}>{t(Strings.assistant_beginner_task)}<span>{`${doneStepCount}/${stepCount}`}</span></div>
          <Progress percent={doneStepCount / stepCount} maxCount={maxCount} curCount={curCount} />
        </div>
        {
          todoConfig.map(item => (
            <div
              className={styles.item}
              onClick={item.onClick}
              id={`VIKA_GUIDE_VIDEO_${item.wizardId}_ITEM`}
              key={item.wizardId}
            >
              <div className={styles.left}>
                {item.leftIcon}
                <span className={styles.info}>
                  <span className={styles.title}>{item.title}</span>
                  {item.isDone ?
                    <span className={styles.doneDesc}>{t(Strings[`wizard_${item.wizardId}_success_message`])}</span> :
                    <span className={styles.defaultDesc}>
                      {item.vCount}
                      <span style={{ marginLeft: 4, display: 'flex', alignItems: 'center' }}>
                        <Image src={GoldImg} width={16} height={16} alt="" />
                      </span>
                    </span>
                  }
                </span>
              </div>
              <span className={styles.right}>{item.rightIcon}</span>
            </div>
          ))
        }
      </ScrollBar>
    );
  }, [onScroll, title, doneStepCount, stepCount, maxCount, curCount, todoConfig]);
  return (
    <>
      <div className={styles.vikabyTodo} id={GUIDE_TASK_CARD_ID} style={{ ...props.style }}>
        <button
          className={classNames(styles.closeIcon, {
            [styles.purpleBg]: isInTop,
            [styles.whiteBg]: !isInTop,
          })}
          onClick={onClose}
        >
          <CloseOutlined />
        </button>
        {renderScrollContent}
      </div>
    </>
  );
};
export const showTodoList = ({ style, ...rest }: { style: React.CSSProperties }) => {
  const div = document.createElement('div');
  document.body.appendChild(div);
  const root = createRoot(div);
  root.render(
    (<Provider store={store}>
      <TaskCard style={{ ...style }} {...rest}/>
    </Provider>));
};

export const destroyTodoList = () => {
  const dom = document.querySelector(`#${GUIDE_TASK_CARD_ID}`);
  if (dom && dom.parentNode) {
    document.body.removeChild(dom.parentNode);
  }
};
