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

import { useUpdateEffect } from 'ahooks';
import classnames from 'classnames';
import dayjs from 'dayjs';
import Image from 'next/image';
import { useRouter } from 'next/router';
import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { LinkButton, Typography, useThemeColors } from '@apitable/components';
import { Api, ApiInterface, integrateCdnHost, Navigation, StoreActions, Strings, t } from '@apitable/core';
import { Message, Modal } from 'pc/components/common';
import { TComponent } from 'pc/components/common/t_component';
import { Router } from 'pc/components/route_manager/router';
import { useQuery, useUserRequest } from 'pc/hooks';
import { getEnvVariables } from 'pc/utils/env';
import { paySystemConfig, SubscribePageType } from './config';
import { SubscribeBar } from './subscribe_bar/subscribe_bar';
import { SubscribeFeatureCard } from './subscribe_feature_card/subscribe_feature_card';
import { SubscribeHeader, SubscribeLevelTab } from './subscribe_header/subscribe_header';
import { SubscribeOffer } from './subscribe_offer/subscribe_offer';
import { SubscribePayBar } from './subscribe_pay_bar/subscribe_pay_bar';
import { SubscribePayMethod } from './subscribe_pay_method/subscribe_pay_method';
import { SubscribeSeat } from './subscribe_seat/subscribe_seat';
import { SubscribeTime } from './subscribe_time/subscribe_time';
import styles from './styles.module.less';

type IQueryOrderPriceResponse = Pick<ApiInterface.IQueryOrderPriceResponse, 'priceOrigin' | 'pricePaid' | 'month'>;

export const SubScribeSystem = () => {
  const [seat, setSeat] = useState<undefined | number>();
  const [subscribeLongs, setSubscribeLongs] = useState<undefined | number>();
  const [seatList, setSeatList] = useState<number[]>([]);
  const [monthPrice, setMonthPrice] = useState<IQueryOrderPriceResponse[]>([]);
  const [pay, setPay] = useState(0);
  const [levelTab, setLevelTab] = useState<SubscribeLevelTab>(SubscribeLevelTab.UNUSED);
  const [loading, setLoading] = useState(false);

  const query = useQuery();
  const router = useRouter();
  const { space_id: spaceId } = router.query as { space_id: string };
  const space = useSelector((state) => state.space);
  const { signOutReq } = useUserRequest();
  const dispatch = useDispatch();
  const priceInfoCache = useRef<Map<number, Map<number, ApiInterface.IQueryOrderPriceResponse>>>();
  const colors = useThemeColors();
  const subscription = useSelector((state) => state.billing.subscription);

  const spaceInfo = space.curSpaceInfo;
  const levelInfo = paySystemConfig[levelTab === SubscribeLevelTab.UNUSED || levelTab == null ? SubscribeLevelTab.SILVER : levelTab];

  useEffect(() => {
    if (seat == null) {
      return;
    }
    const info = priceInfoCache.current?.get(seat);
    if (!info) {
      return;
    }
    setMonthPrice([...info.values()]);
  }, [seat, seatList]);

  useEffect(() => {
    if (!spaceInfo) {
      return;
    }
    if (getPageType() === SubscribePageType.Subscribe) {
      return;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [spaceInfo]);

  useEffect(() => {
    if (getPageType() === SubscribePageType.Subscribe && seatList.length) {
      // The first time a user subscribes, the number of seats recommended for subscription based on the current number of people in the space station
      setSeat(seatList.find((item) => item >= spaceInfo?.seats!) || seatList[seatList.length - 1]);
      return;
    }
    if (getPageType() === SubscribePageType.Renewal && subscription) {
      // For renewal requests, the number of seats does not need to be changed, so the number of seats of the original product can be used.
      setSeat(subscription?.maxSeats);
      return;
    }
    if (!subscription || !seatList.length) {
      return;
    }
    // To upgrade, you should select an option over the original subscription seat
    setSeat(seatList.find((item) => item > subscription.maxSeats) || seatList[seatList.length - 1]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [seatList, spaceInfo?.seats, subscription]);

  useUpdateEffect(() => {
    initVariable();
    if (!spaceId || levelTab === SubscribeLevelTab.UNUSED) {
      return;
    }
    if (levelTab === SubscribeLevelTab.ENTERPRISE) {
      return;
    }
    setLoading(true);
    Api.queryOrderPriceList(levelTab).then((res) => {
      const { data, message, success } = res.data;
      setLoading(false);
      if (!success) {
        Message.warning({ content: message });
        setTimeout(async () => {
          await signOutReq();
          Router.redirect(Navigation.LOGIN, {
            query: {
              reference: location.href,
            },
          });
        }, 1000);
        return;
      }

      const isIncorrectRenewalProduct = () => {
        if (getPageType() !== SubscribePageType.Renewal) {
          return false;
        }
        if (levelInfo.level === paySystemConfig.SILVER.level && subscription?.maxSeats !== 2 && subscription?.maxSeats !== 100) {
          return true;
        }
        return levelInfo.level === paySystemConfig.GOLD.level && subscription?.maxSeats !== 200;
      };

      /**
       * Data structure of priceInfoCache.current:
       * {
       *   [Seats]:{
       *     [Subscription Duration]:{
       *       // Specific product price and other information
       *     }
       *   },
       *   ...........
       * }
       */
      priceInfoCache.current = new Map();
      const seatList: number[] = [];
      // const monthPrice: IQueryOrderPriceResponse[] = [];
      for (const v of data) {
        if (isIncorrectRenewalProduct()) {
          /**
           * The product specifications have been changed so that each product only corresponds to one type of seat,
           * so the logic here is that if the current user's space station seat is different from
           * the number of seats given by the product and the user is in the renewal phase,
           * the user will be prompted that the renewal plan does not exist
           */
          Modal.warning({
            title: t(Strings.renewal_prompt),
            content: t(Strings.renewal_prompt_description),
            okText: t(Strings.upgrade),
            cancelText: t(Strings.cancel),
            hiddenCancelBtn: false,
            onOk: () => {
              location.href = `/space/${space.activeId}/upgrade?pageType=${SubscribePageType.Upgrade}`;
            },
            onCancel: () => {
              Router.push(Navigation.SPACE_MANAGE, { params: { pathInSpace: 'overview' } });
            },
          });
          return;
        }
        if (!priceInfoCache.current.has(v.seat)) {
          priceInfoCache.current.set(v.seat, new Map());
          seatList.push(v.seat);
        }
        const inner = priceInfoCache.current.get(v.seat);
        if (inner && !inner.has(v.month)) {
          inner.set(v.month, v);
        }
      }
      setSeatList(seatList);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [levelTab, spaceId]);

  useEffect(() => {
    Api.spaceInfo(spaceId!).then((res) => {
      const { data, success } = res.data;
      if (success) {
        dispatch(StoreActions.setSpaceInfo(data));
        dispatch(
          StoreActions.updateUserInfo({
            spaceName: data.spaceName,
            spaceLogo: data.spaceLogo,
          }),
        );
      }
    });
  }, [spaceId, dispatch]);

  const getPageType = () => {
    const pageType = query.get('pageType');
    if (!pageType) {
      return SubscribePageType.Subscribe;
    }
    return Number(pageType);
  };

  const initVariable = () => {
    setMonthPrice([]);
    setSeatList([]);
    setSeat(0);
    setSubscribeLongs(6);
  };

  const calcExpireDate = () => {
    if (getPageType() !== SubscribePageType.Renewal) {
      // If it is a subscription, the expiration time should be on top of today's + the time the user subscribed
      return dayjs().add(Number(subscribeLongs), 'month').format('YYYY-MM-DD');
    }
    if (!subscription) {
      return null;
    }
    // If it is a renewal, it should be at the expiration time of the user's original product + the time of the user's renewal
    return dayjs(subscription.deadline).add(Number(subscribeLongs), 'month').format('YYYY-MM-DD');
  };

  if (!levelInfo) {
    Message.error({
      content: t(Strings.subscription_expire_error),
    });
    setTimeout(() => {
      window.location.href = '/workbench';
    }, 2 * 1000);
    return <></>;
  }

  return (
    <div className={styles.container}>
      <SubscribeBar />
      <main className={styles.main}>
        <SubscribeHeader levelTab={levelTab} setLevelTab={setLevelTab} levelInfo={levelInfo} pageType={getPageType()} />
        <SubscribeOffer levelTab={levelTab} />

        {levelTab === 'ENTERPRISE' ? (
          <>
            <div className={styles.enterprise}>
              <Typography variant={'h5'} className={styles.enterpriseDesc1}>
                {t(Strings.contact_model_title)}
              </Typography>
              <Typography variant={'body1'} className={styles.enterpriseDesc2}>
                {t(Strings.custom_enterprise)}
              </Typography>
              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <Image
                  alt={''}
                  style={{ borderRadius: '8px' }}
                  src={integrateCdnHost(getEnvVariables().BILLING_ENTERPRISE_CONTACT_US_QRCODE_IMG!)}
                  width={232}
                  height={232}
                />
              </div>
            </div>
            <div style={{ height: 46 }} />
          </>
        ) : (
          <>
            <div className={styles.productArea}>
              <div className={styles.leftCheckInfo}>
                <div
                  className={classnames({
                    [styles.align]: true,
                    [styles.marginBottom]: getPageType() === SubscribePageType.Renewal,
                  })}
                >
                  <Typography
                    variant={'body2'}
                    className={classnames(styles.checkInfoHead, { [styles.marginTop0]: getPageType() === SubscribePageType.Renewal })}
                    color={colors.secondLevelText}
                  >
                    {t(Strings.plan_model_choose_members)}:
                  </Typography>
                  {getPageType() === SubscribePageType.Renewal ? (
                    <Typography variant={'body2'} style={{ display: 'flex' }} color={colors.thirdLevelText}>
                      <TComponent
                        tkey={t(Strings.renewal_seat_warning)}
                        params={{
                          link: (
                            <LinkButton
                              style={{
                                color: levelInfo.activeColor,
                                borderBottom: 0,
                              }}
                              onClick={() => {
                                window.location.href = window.location.origin + window.location.pathname + `?pageType=${SubscribePageType.Upgrade}`;
                              }}
                            >
                              {t(Strings.click_here)}
                            </LinkButton>
                          ),
                        }}
                      />
                    </Typography>
                  ) : (
                    <div
                      className={levelInfo.level !== paySystemConfig.SILVER.level ? styles.horizontalDisplaySeat : styles.hasMultiSeat}
                      style={{
                        marginBottom: getPageType() === SubscribePageType.Subscribe ? 40 : 0,
                      }}
                    >
                      <SubscribeSeat
                        seatList={seatList}
                        seat={seat}
                        setSeat={setSeat}
                        levelInfo={levelInfo}
                        loading={loading}
                        pageType={getPageType()}
                      />
                      {getPageType() === SubscribePageType.Subscribe && (
                        <Typography variant={'body3'} className={styles.seatNumNote}>
                          <TComponent
                            tkey={t(Strings.plan_model_members_tips)}
                            params={{
                              space_leve: (
                                <LinkButton
                                  color={levelInfo.activeColor}
                                  style={{ marginLeft: 4 }}
                                  onClick={() =>
                                    setLevelTab(levelTab === SubscribeLevelTab.SILVER ? SubscribeLevelTab.GOLD : SubscribeLevelTab.ENTERPRISE)
                                  }
                                >
                                  {levelTab === SubscribeLevelTab.SILVER ? t(Strings.gold) : t(Strings.enterprise)}
                                </LinkButton>
                              ),
                            }}
                          />
                        </Typography>
                      )}
                    </div>
                  )}
                </div>

                <div
                  className={classnames({
                    [styles.align]: true,
                    [styles.marginTop50]: getPageType() === SubscribePageType.Upgrade,
                  })}
                >
                  <Typography
                    variant={'body2'}
                    className={classnames(styles.checkInfoHead, { [styles.marginTop0]: getPageType() === SubscribePageType.Upgrade })}
                    color={colors.secondLevelText}
                  >
                    {t(Strings.plan_model_choose_period)}:
                  </Typography>
                  {
                    <SubscribeTime
                      monthPrice={monthPrice}
                      levelInfo={levelInfo}
                      subscribeLongs={subscribeLongs}
                      setSubscribeLongs={setSubscribeLongs}
                      loading={loading}
                    />
                  }
                </div>
                <Typography variant={'body3'} className={styles.expireTime} color={colors.fc3}>
                  {t(Strings.plan_model_period_tips)}：{calcExpireDate()}
                </Typography>

                <div className={styles.align}>
                  <Typography variant={'body2'} className={styles.checkInfoHead} color={colors.secondLevelText}>
                    {t(Strings.choose_pey_method)}:
                  </Typography>
                  <SubscribePayMethod pay={pay} setPay={setPay} levelInfo={levelInfo} />
                </div>
              </div>
              <SubscribeFeatureCard levelInfo={levelInfo} />
            </div>
            <div className={styles.breakLine} />
            <SubscribePayBar
              priceInfoCache={priceInfoCache}
              seat={seat}
              subscribeLongs={subscribeLongs}
              levelInfo={levelInfo}
              levelTab={levelTab}
              spaceId={spaceId || ''}
              pay={pay}
              pageType={getPageType()}
            />
          </>
        )}
      </main>
    </div>
  );
};
