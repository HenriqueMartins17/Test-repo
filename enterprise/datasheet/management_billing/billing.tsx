import { Table } from 'antd';
import { ColumnProps } from 'antd/es/table';
import dayjs from 'dayjs';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import { isExclusiveLimitedProduct } from 'pc/components/space_manage/space_info/utils';
import React, { FC, useEffect, useMemo, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import { Loading, TextButton, ThemeName } from '@apitable/components';
import { Api, IReduxState, Navigation, Strings, t } from '@apitable/core';
import {
  BronzeDarkFilled,
  BronzeLightFilled,
  EnterpriseDarkFilled,
  EnterpriseLightFilled,
  GoldDarkFilled,
  GoldLightFilled,
  SilverDarkFilled,
  SilverLightFilled,
} from '@apitable/icons';
import { Router } from 'pc/components/route_manager/router';
import { useRequest } from 'pc/hooks';
import styles from './style.module.less';

dayjs.extend(localizedFormat);

export enum LevelType {
  Free = 'Free',
  Plus = 'Plus',
  Starter = 'Starter',
  Business = 'Business',
  Pro = 'Pro',
  Enterprise = 'Enterprise',
}

interface IInvoicesInfo {
  invoiceDate: number;
  amount: number;
  status: string;
  invoicePdf: string;
  invoiceId: string;
}

const isAppSumo = (plan: string | undefined) => {
  if (typeof plan !== 'string') return false;
  return plan.toLowerCase().includes('appsumo');
};

export const ManagementBilling: FC = () => {
  const spaceId = useSelector((state: IReduxState) => state.space.activeId)!;
  const tableRef = useRef<HTMLDivElement>(null);
  const themeName = useSelector((state: IReduxState) => state.theme);
  const { loading, data: planData } = useRequest(() => Api.getSubscript(spaceId));
  const planInfo = useMemo(() => {
    if (!planData) return null;
    // eslint-disable-next-line no-unsafe-optional-chaining
    const { data, success } = planData?.data;
    if (!success) {
      return null;
    }
    return data;
  }, [planData]);

  const { data: invoicesData } = useRequest(() => Api.getInvoices(spaceId));
  const [hasMore, setHasMore] = useState<boolean>(false);
  const [invoicesList, setInvoicesList] = useState<IInvoicesInfo[]>([]);
  useEffect(() => {
    if (!invoicesData) return;
    // eslint-disable-next-line no-unsafe-optional-chaining
    const { data, success } = invoicesData?.data;
    if (!success) {
      return;
    }
    setHasMore(data.hasMore);
    setInvoicesList(data.data);
  }, [invoicesData]);

  function loadMoreInvoices() {
    setInvoicesLoading(true);
    Api.getInvoices(spaceId, invoicesList[invoicesList.length - 1].invoiceId).then((res: any) => {
      if (res.data.success) {
        const { data, success } = res.data;
        if (success) {
          setHasMore(data.hasMore);
          const newInvoicesList = invoicesList.concat(data.data);
          setInvoicesList(newInvoicesList);
        }
        setInvoicesLoading(false);
      }
    });
  }

  function jumpToUpgradePage() {
    Router.push(Navigation.SPACE_MANAGE, { params: { pathInSpace: 'upgrade' } });
  }

  function jumpToChangePlan(action?: string) {
    if (isAppSumo(planInfo?.planName)) {
      window.location.href = 'https://appsumo.com/account/products/';
      return;
    }
    Api.updateBillingSubscription(spaceId, planInfo.subscriptionId, action).then((res: any) => {
      if (res.data.success) {
        window.location.href = res.data.data.url;
      }
    });
  }

  function changePaymentMethod() {
    Api.changePaymentMethod(spaceId).then((res: any) => {
      if (res.data.success) {
        window.location.href = res.data.data.url;
      }
    });
  }

  function editBtnClick(record: any) {
    window.location.href = record.invoicePdf.replace(/\/pdf.*/, '');
  }

  function changePlanUserInfo() {
    Api.changePlanUserInfo(spaceId).then((res: any) => {
      if (res.data.success) {
        window.location.href = res.data.data.url;
      }
    });
  }

  const LevelIcon = (type: string) => {
    if (type.includes('appsumo') || isExclusiveLimitedProduct(type)) {
      return themeName === ThemeName.Light ? <EnterpriseLightFilled /> : <EnterpriseDarkFilled />;
    }
    switch (type) {
      case LevelType.Plus:
      case LevelType.Starter:
        return themeName === ThemeName.Light ? <SilverLightFilled /> : <SilverDarkFilled />;
      case LevelType.Pro:
        return themeName === ThemeName.Light ? <GoldLightFilled /> : <GoldDarkFilled />;
      case LevelType.Enterprise:
      case LevelType.Business:
        return themeName === ThemeName.Light ? <EnterpriseLightFilled /> : <EnterpriseDarkFilled />;
      default:
        return themeName === ThemeName.Light ? <BronzeLightFilled /> : <BronzeDarkFilled />;
    }
  };

  function formatePrice(price: number = 0) {
    return Math.abs(price / 100).toFixed(2);
  }

  const columns: ColumnProps<IInvoicesInfo>[] = [
    {
      title: t(Strings.billing_info_date),
      dataIndex: 'invoiceDate',
      key: 'invoiceDate',
      align: 'left',
      render: (value: number) => {
        return dayjs.unix(value).format('LL');
      },
    },
    {
      title: t(Strings.billing_info_amount),
      dataIndex: 'amount',
      key: 'amount',
      align: 'left',
      render: (value: number) => {
        return value > 0 ? `$${formatePrice(value)}` : `($${formatePrice(value)})`;
      },
    },
    {
      title: t(Strings.billing_info_status),
      dataIndex: 'status',
      key: 'status',
      align: 'left',
    },
    {
      title: t(Strings.billing_info_operation),
      dataIndex: 'operate',
      key: 'operate',
      align: 'left',
      render: (_value: any, record: any) => {
        return (
          <div className={styles.operateBtn}>
            <TextButton color="primary" onClick={() => editBtnClick(record)} size="small">
              {t(Strings.billing_info_view)}
            </TextButton>
          </div>
        );
      },
    },
  ];

  const [invoicesLoading, setInvoicesLoading] = useState<boolean>(false);

  const tableOnScroll = (e: React.UIEvent<HTMLElement, UIEvent>) => {
    if (e.currentTarget.scrollHeight - e.currentTarget.scrollTop === 600 && hasMore) {
      loadMoreInvoices();
    }
  };

  function trnaslateStatus(status: string) {
    switch (status) {
      case 'month':
        return t(Strings.billing_info_month);
      case 'year':
        return t(Strings.billing_info_year);
      default:
        return status;
    }
  }

  return (
    <>
      {' '}
      {loading ? (
        <Loading />
      ) : (
        <div className={styles.bilingContent}>
          <div className={styles.planInfo}>
            <h3>{t(Strings.billing_info_plan_info)}</h3>
            <div className={styles.planInfoContent}>
              <table>
                <tr>
                  <td>{t(Strings.billing_info_plan_name)}</td>
                  <td className={styles.planData}>
                    {LevelIcon(planInfo?.planName)}
                    <p>{planInfo?.planName || 'Free'}</p>
                  </td>
                </tr>
                <tr>
                  <td>{t(Strings.billing_info_plan)}</td>
                  <td className={styles.planData}>
                    {planInfo?.price
                      ? `$${formatePrice(planInfo.price.unitAmount)}/${trnaslateStatus(planInfo.price.interval)}`
                      : `$0.00/${trnaslateStatus('month')}`}
                  </td>
                </tr>
                <tr>
                  <td>{t(Strings.billing_info_next_billing_date)}</td>
                  <td className={styles.planData}>
                    {planInfo && planInfo?.planName !== LevelType.Free
                      ? dayjs.unix(planInfo?.chargedThroughDate).format('LL')
                      : t(Strings.billing_info_no_subscription)}
                  </td>
                </tr>
              </table>
            </div>
            {planInfo?.trial || planInfo?.planName === LevelType.Free ? (
              <TextButton
                color="primary"
                className={styles.upgradeButton}
                onClick={planInfo?.trial ? () => jumpToChangePlan() : () => jumpToUpgradePage()}
              >
                {t(Strings.billing_info_upgrade_page)}
              </TextButton>
            ) : (
              <TextButton color="primary" className={styles.upgradeButton} onClick={() => jumpToChangePlan()}>
                {isExclusiveLimitedProduct(planInfo?.planName) ? '' : t(Strings.billing_info_change_plan)}
              </TextButton>
            )}
          </div>

          {!isAppSumo(planInfo?.planName) && !isExclusiveLimitedProduct(planInfo?.planName) && (
            <>
              <div className={styles.dividing} />
              <div className={styles.payment}>
                <h3>{t(Strings.billing_info_payment)}</h3>
                <table>
                  {planInfo?.paymentMethodDetail && (
                    <tr>
                      <td>{t(Strings.billing_info_payment_method)}</td>
                      <td className={styles.planData}>
                        <div className={styles.settingContent}>
                          {planInfo.paymentMethodDetail.brand ? (
                            <p>
                              {planInfo.paymentMethodDetail.brand} {t(Strings.billing_info_in)} {planInfo.paymentMethodDetail.last4}
                            </p>
                          ) : (
                            <p>{t(Strings.billing_info_no_payment_method)}</p>
                          )}
                          <TextButton color="primary" className={styles.upgradeButton} onClick={() => changePaymentMethod()}>
                            {t(Strings.billing_info_change_method)}
                          </TextButton>
                        </div>
                      </td>
                    </tr>
                  )}
                  {!!planInfo?.interval && (
                    <tr>
                      <td>{t(Strings.billing_info_interval)}</td>
                      <td className={styles.planData}>
                        <div className={styles.settingContent}>
                          <p className={styles.intervalText}>{planInfo.interval}</p>
                          <TextButton color="primary" className={styles.upgradeButton} onClick={() => jumpToChangePlan('interval')}>
                            {t(Strings.billing_change_interval)}
                          </TextButton>
                        </div>
                      </td>
                    </tr>
                  )}
                  <tr>
                    <td>{t(Strings.billing_info_space_credit)}</td>
                    <td className={styles.planData}>
                      <p>${formatePrice(planInfo?.credit)}</p>
                    </td>
                  </tr>
                </table>
              </div>
              {!!planInfo?.billingDetail && (
                <>
                  <div className={styles.dividing} />
                  <div className={styles.billingInfo}>
                    <h3>{t(Strings.billing_info)}</h3>
                    <div className={styles.planInfoContent}>
                      <table>
                        <tr>
                          <td>{t(Strings.billing_info_name)}</td>
                          <td className={styles.planData}>{planInfo.billingDetail.name}</td>
                        </tr>
                        <tr>
                          <td>{t(Strings.billing_info_email)}</td>
                          <td className={styles.planData}>{planInfo.billingDetail.email}</td>
                        </tr>
                      </table>
                    </div>
                    {planInfo?.planName !== LevelType.Free && (
                      <TextButton color="primary" className={styles.upgradeButton} onClick={() => changePlanUserInfo()}>
                        {t(Strings.billing_info_change_info)}
                      </TextButton>
                    )}
                  </div>
                </>
              )}
              <>
                <div className={styles.dividing} />
                <div className={styles.invoices}>
                  <h3>{t(Strings.billing_info_invoice)}</h3>
                  <div className={styles.tableWrapper} ref={tableRef} onScroll={tableOnScroll} style={{ height: 600, overflowY: 'scroll' }}>
                    <Table columns={columns} dataSource={invoicesList} pagination={false} sticky />
                  </div>
                  {invoicesLoading && (
                    <div className={styles.lodingWrapper}>
                      <Loading currentColor />
                      <p>{t(Strings.data_loading)}</p>
                    </div>
                  )}
                </div>
              </>
            </>
          )}
        </div>
      )}
    </>
  );
};
