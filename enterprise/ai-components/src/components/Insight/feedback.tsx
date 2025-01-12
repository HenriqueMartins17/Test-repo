import { Table, Pagination, PaginationProps, Modal, Tabs } from 'antd';
import dayjs from 'dayjs';
import React, { useState, useEffect, useMemo, useRef, useContext } from 'react';
import { Tooltip, useThemeColors, Button, DropdownSelect, IconButton, Typography, Skeleton } from '@apitable/components';
import { Strings, t, integrateCdnHost } from '@apitable/core';
import { CloseOutlined, ArrowUpFilled, ArrowDownFilled, CommentOutlined, LikeFilled, DislikeFilled, UserFilled } from '@apitable/icons';
import style from './style.module.less';
import { DrawerWrapper, Avatar, AvatarSize } from '@/components';
import { ChatList } from '@/components/Chat/List';
import * as Shared from '@/shared';
import { ChatType, IChatListItem } from '@/shared/types/chat';
import { getTypeLabel } from '@/shared/utils';
import { convertMessageList2ChatList } from '@/shared/utils/message';

interface IInsightProps {
  id: string;
  page: number;
  setPage: (page: number) => void;
  total: number;
  setTotal: (total: number) => void;
}
export function Feedback(props: IInsightProps) {
  const context = useContext(Shared.AIContext);
  const { page, setPage, total, setTotal } = props;
  const colors = useThemeColors();
  const [list, setList] = useState<Shared.IAIFeedbackDetail[]>([]);
  const [messageList, setMessageList] = useState<IChatListItem[]>([]);
  const [state, setState] = useState<Shared.AIFeedbackState>(Shared.AIFeedbackState.Unprocessed);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(true);
  const [feedBackIndex, setFeedBackIndex] = useState<number>(-1);
  const ref = useRef<HTMLDivElement>(null);

  const feedback = list[feedBackIndex];

  const fetchList = async () => {
    try {
      setLoading(true);
      const res = await context.api.getAIFeedback(props.id, {
        pageNum: page,
        pageSize: 10,
        state,
      });
      setList(res.data.fb);
      setTotal(res.data.total);
    } finally {
      setLoading(false);
    }
  };

  const fetchDetail = async () => {
    if (!loading && feedback) {
      try {
        setDetailLoading(true);
        const res = await context.api.getChatHistoryList(props.id, {
          conversationId: feedback.conversationId,
          trainingId: feedback.trainingId,
        });
        const data = convertMessageList2ChatList(res.data.data);
        setMessageList(data);
        setTimeout(() => {
          const element = ref.current?.querySelector(`[data-index="${feedback.messageIndex}"]`) as HTMLDivElement;
          if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
          }
        }, 200);
      } finally {
        setDetailLoading(false);
      }
    }
  };

  const updateState = async (state: Shared.AIFeedbackState) => {
    try {
      setLoading(true);
      await context.api.updateAIFeedback(props.id, feedback.id, {
        state,
      });
      setFeedBackIndex(-1);
      fetchList();
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();

  }, [page, state]);

  const detail = (list[feedBackIndex] || {}) as Shared.IAIFeedbackDetail;

  useEffect(() => {
    if (feedBackIndex !== -1 && detail) {
      fetchDetail();
    }
  }, [feedBackIndex, list]);

  const messages = useMemo(() => {
    if (detail && messageList && messageList.length) {
      let idx = -1;
      const data = messageList.map((item, index) => {
        if (item.type === ChatType.Bot) {
          if (++idx === detail.messageIndex) {
            return {
              ...item,
              feedback: {
                comment: detail.comment,
                like: detail.isLike ? Shared.AIFeedbackType.Like : Shared.AIFeedbackType.DisLike,
              },
            };
          }
        }
        return item;
      });
      return data as IChatListItem[];
    }
    return [];
  }, [messageList, detail]);

  const modalTitle = useMemo(() => {
    if (feedback) {
      if (feedback.comment) {
        return feedback.comment;
      }
      let idx = -1;
      for (let index = 0; index < messageList.length; index++) {
        const item = messageList[index];
        if (item.type === ChatType.Bot) {
          if (++idx === feedback.messageIndex) {
            return item.content;
          }
        }
      }
    }
    return '';
  }, [feedback, messageList]);
  // Strings.ai_agent_conversation_log

  return (
    <div className={style.insight}>
      <div className={style.insightFilter}>
        <div className={style.insightFilterItem}>
          <span className={style.insightFilterItemText}>{t(Strings.ai_feedback_state)}</span>
          <DropdownSelect
            onSelected={(v) => {
              setState(v.value as Shared.AIFeedbackState);
            }}
            value={state}
            triggerStyle={{ width: 200 }}
            options={[
              { label: t(Strings.ai_feedback_state_unprocessed), value: Shared.AIFeedbackState.Unprocessed },
              { label: t(Strings.ai_feedback_state_processed), value: Shared.AIFeedbackState.Processed },
              { label: t(Strings.ai_feedback_state_ignore), value: Shared.AIFeedbackState.Ignore },
            ]}
          />
        </div>
      </div>
      {loading ? (
        <div style={{ padding: 16 }}>
          <Skeleton height="24px" />
          <Skeleton count={2} style={{ marginTop: '24px' }} height="80px" />
        </div>
      ) : (
        <Table
          loading={loading}
          bordered={false}
          pagination={false}
          dataSource={list}
          // 点击表格行时触发
          onRow={(record, index) => {
            return {
              onClick: () => {
                if (index !== undefined) setFeedBackIndex(index);
              },
            };
          }}
          columns={[
            {
              title: t(Strings.ai_feedback_time),
              dataIndex: 'created',
              width: 180,
              render: (v: string) => dayjs(Number(v)).format('YYYY-MM-DD HH:mm:ss'),
            },
            {
              title: t(Strings.ai_feedback_user),
              dataIndex: 'creator',
              width: 160,
              render: (v: string, record) => {
                return (
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    {record.memberId ? (
                      <>
                        <Avatar
                          id={record.memberId}
                          src={integrateCdnHost(record.avatar)}
                          title={v}
                          size={AvatarSize.Size32}
                          style={{ marginRight: 8 }}
                        />
                        <span>{v}</span>
                      </>
                    ) : (
                      <div className={style.avatar}>
                        <UserFilled size={32} color={colors.textStaticPrimary} />
                        <span>{t(Strings.ai_feedback_anonymous)}</span>
                      </div>
                    )}
                  </div>
                );
              },
            },
            { title: t(Strings.ai_feedback_comment), dataIndex: 'comment', ellipsis: true },
            {
              title: t(Strings.ai_feedback_appraisal),
              dataIndex: 'isLike',
              width: 160,
              render: (v: boolean) => {
                return (
                  <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
                    {v ? <LikeFilled color={colors.textBrandDefault} /> : <DislikeFilled color={colors.textDangerDefault} />}
                  </div>
                );
              },
            },
            {
              title: t(Strings.ai_feedback_state),
              dataIndex: 'state',
              width: 100,
              render: (v: Shared.AIFeedbackState) => {
                if (v === Shared.AIFeedbackState.Unprocessed) {
                  return '-';
                }
                if (v === Shared.AIFeedbackState.Processed) {
                  return t(Strings.ai_feedback_state_processed);
                }
                return t(Strings.ai_feedback_state_ignore);
              },
            },
          ]}
        />
      )}

      <Modal
        open={feedBackIndex !== -1}
        footer={null}
        closable={false}
        className={style.modal}
        width={900}
        zIndex={300}
        onCancel={() => {
          setFeedBackIndex(-1);
        }}
        title={
          <div className={style.modalHeader}>
            <div className={style.modalHeaderTitle}>
              <CommentOutlined />
              <Typography ellipsis variant="body3">
                {modalTitle}
              </Typography>
            </div>
            <div className={style.modalHeaderTitleButton}>
              <Tooltip content={t(Strings.previous_record_plain)}>
                <span>
                  <IconButton
                    component="button"
                    shape="square"
                    icon={ArrowUpFilled}
                    disabled={page === 1 && feedBackIndex === 0}
                    onClick={() => {
                      if (feedBackIndex === 0) {
                        setPage(page - 1);
                        setFeedBackIndex(9);
                      } else {
                        setFeedBackIndex(feedBackIndex - 1);
                      }
                    }}
                  />
                </span>
              </Tooltip>
              <Tooltip content={t(Strings.next_record_plain)}>
                <span>
                  <IconButton
                    component="button"
                    shape="square"
                    icon={ArrowDownFilled}
                    disabled={(page - 1) * 10 + feedBackIndex >= total - 1}
                    onClick={() => {
                      if (feedBackIndex === 9) {
                        setPage(page + 1);
                        setFeedBackIndex(0);
                      } else {
                        setFeedBackIndex(feedBackIndex + 1);
                      }
                    }}
                  />
                </span>
              </Tooltip>
              <IconButton
                component="button"
                shape="square"
                icon={CloseOutlined}
                onClick={() => {
                  setFeedBackIndex(-1);
                }}
              />
            </div>
          </div>
        }
      >
        {detailLoading || !feedback ? (
          <div style={{ padding: 16 }}>
            <Skeleton height="24px" />
            <Skeleton count={2} style={{ marginTop: '24px' }} height="80px" />
          </div>
        ) : (
          <div className={style.modalContent}>
            <div className={style.modalContentMessage} ref={ref}>
              <ChatList
                ignoreGrid
                preview
                chatList={messages}
                userInfo={{
                  memberId: feedback.memberId,
                  avatar: feedback.avatar,
                  nickName: feedback.creator,
                }}
              />
            </div>
            <div className={style.modalContentInfo}>
              <div className={style.modalContentInfoAvatar}>
                {detail.memberId ? (
                  <Avatar
                    id={detail.memberId}
                    src={integrateCdnHost(detail.avatar)}
                    title={detail.creator}
                    size={AvatarSize.Size32}
                    style={{ marginRight: 8 }}
                  />
                ) : (
                  <UserFilled size={32} color={colors.textStaticPrimary} />
                )}
                <Typography variant="body2">{detail.memberId ? detail.creator : t(Strings.ai_feedback_anonymous)}</Typography>
              </div>
              <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                  {t(Strings.ai_session_time)}
                </Typography>
                <Typography variant="h7">{dayjs(Number(detail.conversationTime)).format('YYYY-MM-DD HH:mm:ss')}</Typography>
              </div>
              <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                AI Model
                </Typography>
                <Typography variant="h7">{detail.aiModel}</Typography>
              </div>
              <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                Bot Type
                </Typography>
                <Typography variant="h7">{getTypeLabel(detail.botType)}</Typography>
              </div>
              {state === Shared.AIFeedbackState.Unprocessed && (
                <div className={style.modalContentInfoAction}>
                  <Typography variant="body4">{t(Strings.ai_mark_state)}</Typography>
                  <div className={style.modalContentInfoActionItem}>
                    <Button loading={loading} onClick={() => updateState(Shared.AIFeedbackState.Processed)} color="primary">
                      {t(Strings.ai_feedback_state_processed)}
                    </Button>
                    <Button loading={loading} onClick={() => updateState(Shared.AIFeedbackState.Ignore)} variant="jelly" color="primary">
                      {t(Strings.ai_feedback_state_ignore)}
                    </Button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
        {/* updateAIFeedback */}
      </Modal>
    </div>
  );
}
