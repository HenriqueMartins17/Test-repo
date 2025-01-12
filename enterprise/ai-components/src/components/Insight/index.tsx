import { Table, Pagination, PaginationProps, Modal, Tabs } from 'antd';
import React, { useState, useEffect, useMemo, useRef, useContext } from 'react';
import { Strings, t } from '@apitable/core';
import { Conversation } from './conversation';
import { Feedback } from './feedback';
import style from './style.module.less';
import { DrawerWrapper } from '@/components/DrawerWrapper';

interface IInsightProps {
  visible: boolean;
  close: () => void;
  id: string;
}
export function Insight(props: IInsightProps) {
  const [activeTab, setActiveTab] = useState<'conversation' | 'feedback'>('feedback');
  const { visible, close } = props;
  const showTotal: PaginationProps['showTotal'] = (total) => t(Strings.ai_feedback_insight_pagination_total, { total });
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  return (
    <DrawerWrapper
      open={visible}
      close={close}
      classNames={style.insight}
      config={{ title: t(Strings.ai_toolbar_insight_text), documentLink: '', modalWidth: 1000 }}
      footer={
        total > 10 ? (
          <div className={style.insightFooter}>
            <Pagination
              showSizeChanger={false}
              size="small"
              showTotal={showTotal}
              current={page}
              pageSize={10}
              total={total}
              onChange={(n) => {
                setPage(n);
              }}
            />
          </div>
        ) : (
          <></>
        )
      }
    >
      <Tabs
        activeKey={activeTab}
        onChange={(key) => {
          setActiveTab(key as any);
          setPage(1);
        }}
        destroyInactiveTabPane
        items={[
          {
            key: 'feedback',
            label: t(Strings.ai_agent_feedback),
            children: (
              <Feedback
                id={props.id}
                total={total}
                setTotal={setTotal}
                page={page}
                setPage={setPage}
              />
            )
          },
          {
            key: 'conversation',
            label: t(Strings.ai_agent_conversation_log),
            children: (
              <Conversation
                id={props.id}
                total={total}
                setTotal={setTotal}
                page={page}
                setPage={setPage}
              />
            )
          },
        ]}
      />

    </DrawerWrapper>
  );
}
