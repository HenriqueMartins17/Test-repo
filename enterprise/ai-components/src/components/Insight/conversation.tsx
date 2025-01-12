import { Table, Pagination, PaginationProps, Modal, Tabs } from 'antd';
import dayjs from 'dayjs';
import React, { useState, useEffect, useMemo, useRef, useContext } from 'react';
import { Tooltip, useThemeColors, Button, DropdownSelect, IconButton, Typography, Skeleton } from '@apitable/components';
import { Strings, t, integrateCdnHost } from '@apitable/core';
import { CloseOutlined, ArrowUpFilled, ArrowDownFilled, CommentOutlined, LikeFilled, DislikeFilled, UserFilled } from '@apitable/icons';
import style from './style.module.less';
import { Avatar, AvatarSize } from '@/components';
import { ChatList } from '@/components/Chat/List';
import * as Shared from '@/shared';
import { ChatType, IChatListItem } from '@/shared/types/chat';
import { convertMessageList2ChatList } from '@/shared/utils/message';

interface IInsightProps {
  id: string;
  page: number;
  setPage: (page: number) => void;
  total: number;
  setTotal: (total: number) => void;
}
export function Conversation(props: IInsightProps) {
  const context = useContext(Shared.AIContext);
  const { page, setPage, total, setTotal } = props;
  const colors = useThemeColors();
  const [list, setList] = useState<Shared.IConversationHistoryItem[]>([]);
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
      const res = await context.api.getInsightConversations(props.id, page, 10);
      setList(res.data.records);
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
          conversationId: feedback.id,
          trainingId: feedback.trainingId,
        });
        const data = convertMessageList2ChatList(res.data.data);
        setMessageList(data);
        setTimeout(() => {
          ref.current?.scrollTo({ top: ref.current?.scrollHeight });
        }, 200);
      } finally {
        setDetailLoading(false);
      }
    }
  };

  useEffect(() => {
    fetchList();

  }, [page, state]);

  const detail = (list[feedBackIndex] || {}) as Shared.IConversationHistoryItem;

  useEffect(() => {
    if (feedBackIndex !== -1 && detail) {
      fetchDetail();
    }
  }, [feedBackIndex, list]);

  const modalTitle = useMemo(() => {
    if (feedback) {
      return feedback.title;
    }
    return '';
  }, [feedback, messageList]);
  // Strings.ai_agent_conversation_log

  return (
    <div className={style.insight}>
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
              dataIndex: 'user',
              width: 160,
              render: (v: string, record) => {
                return (
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    {record.user ? (
                      <>
                        <Avatar
                          id="1"
                          src={record.user.avatar}
                          title={record.user.name}
                          size={AvatarSize.Size32}
                          style={{ marginRight: 8 }}
                        />
                        <span>{record.user.name}</span>
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
            { title: t(Strings.ai_agent_conversation_title), dataIndex: 'title', ellipsis: true },
            {
              title: t(Strings.ai_agent_message_consumed),
              dataIndex: 'transaction.totalAmount',
              width: 160,
              render: (v: string, record) => {
                return record.transaction.totalAmount;
              }
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
                chatList={messageList}
                userInfo={feedback.user ? {
                  avatar: feedback.user.avatar,
                  nickName: feedback.user.name,
                } : undefined}
              />
            </div>
            <div className={style.modalContentInfo}>
              <div className={style.modalContentInfoAvatar}>
                {detail.user ? (
                  <Avatar
                    src={integrateCdnHost(detail.user.avatar)}
                    title={detail.user.name}
                    size={AvatarSize.Size32}
                    style={{ marginRight: 8 }}
                  />
                ) : (
                  <UserFilled size={32} color={colors.textStaticPrimary} />
                )}
                <Typography variant="body2">{detail.user ? detail.user.name : t(Strings.ai_feedback_anonymous)}</Typography>
              </div>
              <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                  {t(Strings.ai_session_time)}
                </Typography>
                <Typography variant="h7">{dayjs(Number(detail.created)).format('YYYY-MM-DD HH:mm:ss')}</Typography>
              </div>
              {/* <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                AI Model
                </Typography>
                <Typography variant="h7">{detail.origin}</Typography>
              </div> */}
              {/* <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                Bot Type
                </Typography>
                <Typography variant="h7">{getTypeLabel(detail.botType)}</Typography>
              </div> */}
              <div className={style.modalContentInfoItem}>
                <Typography style={{ width: 100 }} variant="body2">
                  { t(Strings.ai_agent_message_consumed) }
                </Typography>
                <Typography variant="h7">{detail.transaction.totalAmount}</Typography>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
