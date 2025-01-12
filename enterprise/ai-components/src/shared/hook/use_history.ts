import { useEffect, useState } from 'react';
import { Response } from '@/shared/api';
import {
  IPageResponse,
  IConversationHistoryItem,
} from '@/shared/types';

interface IParams {
  getConversationHistory: (pageNum: number, pageSize: number) => Response<IPageResponse<IConversationHistoryItem>>;
}

export interface IHistoryState {
  current: IConversationHistoryItem;
  loading: boolean;
  hasNextPage: boolean;
  open: boolean;
  data: IConversationHistoryItem[];
  setOpen: (open: boolean) => void;
  setCurrent: (item: IConversationHistoryItem) => void;
  refresh: () => Promise<void>;
  append: () => Promise<void>;
}

export const useHistory = (params: IParams): IHistoryState => {
  const [current, setCurrent] = useState<IConversationHistoryItem>(null);
  const [open, setOpen] = useState(false);
  const { getConversationHistory } = params;
  const [pageNum, setPageNum] = useState(1);
  const pageSize = 30;
  // const [pageSize, setPageSize] = useState(30);
  const [loading, setLoading] = useState(false);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [data, setData] = useState<IConversationHistoryItem[]>([]);

  const append = async () => {
    if (open) {
      const records = await fetch(pageNum + 1);
      setData(data.concat(records));
    }
  };

  const fetch = async (page = 1) => {
    setLoading(true);
    const res = await getConversationHistory(page, pageSize);
    setPageNum(page);
    setLoading(false);
    setHasNextPage(res.data.hasNextPage);
    return res.data.records;
  };

  const refresh = async () => {
    if (open) {
      setData([]);
      const records = await fetch(1);
      setData(records);
    }
  };

  useEffect(() => {
    if (open) {
      refresh();
    } else {
      setData([]);
    }
  }, [open]);

  return {
    open,
    data,
    setOpen,
    hasNextPage,
    loading,
    current,
    setCurrent,
    refresh,
    append,
  };
};
