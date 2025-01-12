import classNames from 'classnames';
import { useThemeColors } from '@apitable/components';
import { LikeOutlined, LikeFilled, DislikeFilled, DislikeOutlined, LinkOutlined, FormOutlined } from '@apitable/icons';
import FeedbackItem from './feedback';
import ToolBtn from './tool_btn';
import { IMessageProps } from '.';
import styles from './style.module.less';
import type { ToolBarState } from '@/components/Chat/List';
import * as Shared from '@/shared';
import { ChatType } from '@/shared/types/chat';

export interface IToolBarStateProps extends ToolBarState {
  toolActiveId?: string | null;
  setToolActiveId?: (id: string | null) => void;
  feedback?: Shared.IAIFeedback;
}

export default function MessageToolBar(props: IMessageProps & { toolBarState: IToolBarStateProps } & { index: number }) {
  const { role, index, toolBarState } = props;
  const { config, isIdle, formName, sendLinkMessage, startAIFormMode } = toolBarState;
  // const { context, sendLinkMessage, startAIFormMode } = useAIContext();
  const colors = useThemeColors();

  const likeId = `like-${index}`;
  const dislikeId = `dislike-${index}`;

  const feedbackActive = toolBarState.toolActiveId === likeId || toolBarState.toolActiveId === dislikeId;

  const record = toolBarState.feedback;

  if (role === ChatType.User || !toolBarState.setToolActiveId) {
    return null;
  }

  const renderSendUrlBtn = () => {
    if (config.type === Shared.AIType.Qa) {
      if (config.isEnableOpenUrl) {
        return (
          <ToolBtn onClick={sendLinkMessage} tooltip={config.openUrlTitle}>
            <LinkOutlined />
          </ToolBtn>
        );
      }
    }
    return null;
  };

  const renderAIFormBtn = () => {
    if (config.type === Shared.AIType.Qa) {
      if (config.isEnableCollectInformation) {
        return (
          <ToolBtn
            onClick={async () => {
              await startAIFormMode();
            }}
            tooltip={formName}
          >
            <FormOutlined />
          </ToolBtn>
        );
      }
    }
    return null;
  };

  return (
    <div
      className={classNames({
        [styles.toolbar]: true,
        [styles.toolbarActive]: !!feedbackActive || !!record,
      })}
    >
      {record ? (
        <>
          <FeedbackItem status={record.isLike ? Shared.AIFeedbackType.Like : Shared.AIFeedbackType.DisLike}>
            {record.isLike ? <LikeFilled color={colors.textBrandDefault} /> : <DislikeFilled color={colors.textDangerDefault} />}
          </FeedbackItem>
        </>
      ) : (
        <>
          <FeedbackItem
            id={likeId}
            feedbackActiveId={toolBarState.toolActiveId || null}
            setFeedbackActiveId={toolBarState.setToolActiveId}
            sendFeedback={async (message) => {
              if (toolBarState.sendFeedback) {
                await toolBarState.sendFeedback(index, message, Shared.AIFeedbackType.Like);
              }
            }}
          >
            <LikeOutlined />
          </FeedbackItem>
          <FeedbackItem
            id={dislikeId}
            feedbackActiveId={toolBarState.toolActiveId || null}
            setFeedbackActiveId={toolBarState.setToolActiveId}
            sendFeedback={async (message) => {
              if (toolBarState.sendFeedback) {
                await toolBarState.sendFeedback(index, message, Shared.AIFeedbackType.DisLike);
              }
            }}
          >
            <DislikeOutlined />
          </FeedbackItem>
          {isIdle && (
            <>
              {renderSendUrlBtn()}
              {renderAIFormBtn()}
            </>
          )}
        </>
      )}
    </div>
  );
}
