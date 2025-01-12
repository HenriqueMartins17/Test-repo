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

import { useMount, useToggle } from 'ahooks';
import { Table } from 'antd';
import { ColumnProps } from 'antd/es/table';
import classNames from 'classnames';
import dayjs from 'dayjs';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';
import Image from 'next/image';
import * as React from 'react';
import { FC, useEffect, useState } from 'react';
import { Button, Pagination, Skeleton, useThemeColors } from '@apitable/components';
import { Api, ConfigConstant, IPageDataBase, isVCode, Strings, t } from '@apitable/core';
import { QuestionCircleOutlined, ChevronRightOutlined } from '@apitable/icons';
import { WithTipTextInput } from 'pc/components/common/input/with_tip_input';
import { NormalModal } from 'pc/components/common/modal/normal_modal';
// eslint-disable-next-line no-restricted-imports
import { Tooltip } from 'pc/components/common/tooltip';
import { useBilling } from 'pc/hooks/use_billing';
import { useRequest } from 'pc/hooks/use_request';
import { getEnvVariables } from 'pc/utils/env';
import GoldImg from 'static/icon/workbench/workbench_account_gold_icon.png';
import LeftCardBg from 'static/icon/workbench/workbench_account_left_bj.png';
import AccountLogo from 'static/icon/workbench/workbench_account_logo.svg';
import { showExchangeSuccess } from './exchange_success';
import styles from './style.module.less';

dayjs.extend(utc);
dayjs.extend(timezone);

interface IIntegralInfo {
  totalIntegral: number;
}

interface IParams {
  name: string;
}

interface IRecord {
  action: string;
  actionName: string;
  alterType: number;
  alterValue: string;
  createdAt: string;
  params: null | IParams;
}

interface IPageData extends IPageDataBase {
  records: IRecord[];
}

const ErrContant = {
  FormatErr: t(Strings.invalid_redemption_code_entered),
  EmptyErr: t(Strings.no_redemption_code_entered),
};
export const AccountWallet: FC = () => {
  const colors = useThemeColors();
  const AlterTypeInfo = {
    0: { color: colors.warningColor, text: '+' },
    1: { color: colors.errorColor, text: '-' },
  };
  // Income and expenditure records
  const [pageNo, setPageNo] = useState(1);
  const [pageData, setPageData] = useState<IPageData | null>(null);
  // Subscription Information
  const { getUserIntegral } = useBilling();
  const { data: integralInfo, run: getUserIntegralRun, loading: cardLoading } = useRequest<IIntegralInfo>(getUserIntegral, { manual: true });
  const [inputErr, setInputErr] = useState('');
  const [inputText, setInputText] = useState('');
  const [exchangeModal, { set: setExchangeModal }] = useToggle(false);
  const { run: getRecordsData, loading: recordsGetting } =
    useRequest((pageNo: number) => Api.getUserIntegralRecords(pageNo).then(res => {
      const { data, success } = res.data;
      if (success) {
        setPageData(data);
      }
    }), { manual: true });
  const { run: exchange, loading: exchanging } =
    useRequest((code: string) => Api.vCodeExchange(code).then(res => {
      const { message, success, data } = res.data;
      if (success) {
        setExchangeModal(false);
        showExchangeSuccess({ amount: data, refreshData: refreshData });
      } else {
        setInputErr(message);
      }
    }), { manual: true });
  const env = getEnvVariables();

  useMount(() => {
    getUserIntegralRun();
  });
  useEffect(() => {
    pageNo && getRecordsData(pageNo);
  }, [pageNo, getRecordsData]);
  const refreshData = () => {
    pageNo === 1 ? getRecordsData(pageNo) : setPageNo(1);
    getUserIntegralRun();
  };
  const inputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.trim();
    setInputText(val);
    setInputErr('');
  };
  const cancelExchange = () => {
    setExchangeModal(false);
  };
  const confirmExchange = () => {
    if (inputErr) return;
    if (!inputText) {
      setInputErr(ErrContant.EmptyErr);
      return;
    }
    if (!isVCode(inputText)) {
      setInputErr(ErrContant.FormatErr);
      return;
    }
    exchange(inputText.trim());
  };

  const columns: ColumnProps<IRecord>[] = [
    {
      title: t(Strings.behavior_type),
      dataIndex: 'action',
      key: 'action',
      render: (value, record) => t(Strings[value], { name: record.params && record.params.name ? record.params.name : '' }),
      align: 'left',
      ellipsis: true,
    },
    {
      title: t(Strings.time),
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: value => dayjs(value).format(t(Strings.time_format_year_month_and_day)),
      align: 'left',
      width: 200,
    },
    {
      title: t(Strings.remarks),
      dataIndex: 'alterType',
      key: 'alterType',
      render: (value, record) => {
        return (
          <div className={styles.money} style={{ color: AlterTypeInfo[value].color }}>
            {AlterTypeInfo[value].text}
            {Number(record.alterValue).toLocaleString()}
            <span style={{ marginLeft: 4, display: 'flex', alignItems: 'center' }}><Image alt='gold' src={GoldImg} width={16} height={16} /></span>
          </div>
        );
      },
      align: 'left',
      width: 150,
    },
  ];
  const inLoading = recordsGetting || !pageData;
  const shouldShowRecords = !inLoading;
  return (
    <div className={styles.accountWalletWrapper}>
      {
        env.ACCOUNT_WALLET_HELP_URL && <div className={styles.title}>
          {t(Strings.account_wallet)}
          <a href={env.ACCOUNT_WALLET_HELP_URL} target='_blank' rel='noreferrer'><QuestionCircleOutlined /></a>
        </div>
      }
      <div className={styles.content}>
        <div className={styles.bigCardWrap}>
          <div className={styles.cardWrap}>
            {
              !cardLoading &&
              <div
                className={classNames(styles.card, styles.curCard)}
                style={{
                  backgroundColor: cardLoading ? colors.lowestBg : colors.primaryColor,
                  position: 'relative',
                }}
              >
                <Image src={LeftCardBg} layout={'fill'} alt="" />
                <div className={styles.cardTop}>
                  <div className={styles.cardTopLeft}>
                    <div className={styles.curVTitle}>{t(Strings.current_v_coins)}</div>
                    <div
                      className={styles.curV}>
                      <span>{integralInfo?.totalIntegral.toLocaleString()}</span>
                      {t(Strings.v_coins)}
                    </div>
                  </div>
                  <div className={styles.cardTopRight}>
                    <Button
                      color='primary'
                      size='small'
                      shape='round'
                      onClick={() => setExchangeModal(true)}
                      style={{ position: 'absolute', top: 0, right: 0, fontSize: '14px', lineHeight: '14px' }}
                    >
                      {t(Strings.redemption_code_button)}<ChevronRightOutlined size={8} color={colors.black[50]} />
                    </Button>
                  </div>
                </div>
                <div className={styles.cardBottom}>
                  <Tooltip title={t(Strings.get_v_coins)}>
                    <span><AccountLogo />{t(Strings.how_to_get_v_coins)}</span>
                  </Tooltip>
                </div>
              </div>
            }
          </div>
          {!cardLoading &&
            <div
              className={classNames(styles.cardWrap, styles.rightCardWrap)}>{t(Strings.stay_tuned_for_more_features)}
            </div>
          }
        </div>
        <div className={styles.subTitle}>{t(Strings.income_expenditure_records)}</div>
        {
          inLoading && (
            <div className={styles.skeleton}>
              <Skeleton count={1} width='38%' />
              <Skeleton count={2} />
              <Skeleton count={1} width='61%' />
            </div>
          )
        }
        {
          pageData && shouldShowRecords &&
          <>
            <Table
              columns={columns}
              dataSource={pageData.records}
              pagination={false}
              rowKey={record => record.createdAt}
            />
            {
              pageData.total !== 0 &&
              <div className={styles.pagination}>
                <Pagination
                  current={pageNo}
                  total={pageData!.total}
                  pageSize={ConfigConstant.USER_INTEGRAL_RECORDS_PAGE_SIZE}
                  onChange={pageNo => setPageNo(pageNo)}
                />
              </div>
            }

          </>
        }
      </div>
      {
        <NormalModal
          title={t(Strings.redemption_code)}
          maskClosable={false}
          visible={exchangeModal}
          centered
          okText={t(Strings.exchange)}
          onCancel={cancelExchange}
          onOk={confirmExchange}
          okButtonProps={{ loading: exchanging, disabled: exchanging || !inputText }}
          className={styles.exchangeCodeModal}
        >
          <div className={styles.exchangeTip}>{t(Strings.exchange_code_times_tip)}</div>
          <WithTipTextInput
            placeholder={t(Strings.entered_the_wrong_redemption_code)}
            onChange={inputChange}
            className={styles.input}
            error={Boolean(inputErr)}
            helperText={inputErr}
            block
          />
        </NormalModal>
      }
    </div>
  );
};
