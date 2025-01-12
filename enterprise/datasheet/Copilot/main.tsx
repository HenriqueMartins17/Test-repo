import { Popover } from 'antd';
import classNames from 'classnames';
import { useState } from 'react';
import { useSelector } from 'react-redux';
import {
  Chat,
  ConversationStatus,
  TextArea,
  AIType,
  TextButton,
  useCopilotContext,
} from '@apitable/ai';
import { Typography } from '@apitable/components';
import { t, Strings } from '@apitable/core';
import { ClearOutlined, ChevronDownOutlined, ChevronUpOutlined, LoadingOutlined } from '@apitable/icons';
import { AGENT_LIST } from './index';
import style from './index.module.less';

export const CopilotMain = () => {
  const [open, setOpen] = useState(false);
  const hide = () => {
    setOpen(false);
  };
  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
  };
  const { context } = useCopilotContext();

  const user = useSelector((state) => state.user);
  // const hook = useAIContext();
  const disabled = context.loading || context.state.conversationStatus !== ConversationStatus.Idle;
  const agentType = AGENT_LIST.find((item) => item.value === context.data?.latestConversation?.type);

  return (
    <>
      {context.loading ? (
        <div className={style.pageLoading}>
          <LoadingOutlined className="circle-loading" />
          {t(Strings.ai_page_loading_text)}
        </div>
      ) : (
        <Chat
          ignoreGrid
          ignoreBackground
          isFusionMode
          chatList={context.state.chatList}
          conversationStatus={context.state.conversationStatus}
          sendMessage={context.sendMessage}
          stopConversation={context.stopConversation}
          userInfo={user.info}
        />
      )}
      <div className={style.footer}>
        <div className={style.footerToolbar}>
          {agentType && (
            <Popover
              overlayClassName={style.footerToolbarSelectPopover}
              showArrow={false}
              content={
                <>
                  {AGENT_LIST.map((item, index) => (
                    <div
                      className={classNames(style.footerToolbarSelectItem, {
                        [style.disabled]: context.loading,
                        [style.active]: agentType.value === item.value,
                      })}
                      key={index}
                      onClick={() => {
                        if (!context.loading && agentType.value !== item.value) {
                          context.setAgent(item.value as AIType);
                          hide();
                        }
                      }}
                    >
                      <Typography variant="body3" className={style.footerToolbarSelectItemTitle}>
                        {item.label}
                      </Typography>
                      <Typography variant="body4" className={style.footerToolbarSelectItemDesc}>
                        {item.desc}
                      </Typography>
                    </div>
                  ))}
                </>
              }
              trigger={context.loading ? [] : ['click']}
              open={open}
              onOpenChange={handleOpenChange}
            >
              <div
                className={classNames(style.footerToolbarSelect, {
                  [style.disabled]: context.loading,
                })}
              >
                <Typography variant='body3'>
                  { agentType?.label }
                </Typography>

                { open ? <ChevronUpOutlined /> : <ChevronDownOutlined /> }
              </div>
            </Popover>
          )}
          <TextButton
            style={{ maxHeight:24 }}
            disabled={context.loading}
            onClick={context.newConversation}
            prefixIcon={<ClearOutlined />}
          >
            {t(Strings.ai_new_conversation_btn_text)}
          </TextButton>
        </div>
        <div className={style.footerTextArea}>
          <TextArea sendMessage={context.sendMessage} disabled={disabled} />
        </div>
      </div>

    </>
  );
};
