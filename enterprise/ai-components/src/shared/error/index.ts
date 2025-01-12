import { Strings, t } from '@apitable/core';
import { BillingErrorCode } from '@/shared/enum';

export class BillingUsageOverLimitError extends Error {
  public code: BillingErrorCode;
  constructor() {
    super(t(Strings.subscribe_credit_usage_over_limit));
    this.code = BillingErrorCode.OVER_LIMIT;
  }
}
