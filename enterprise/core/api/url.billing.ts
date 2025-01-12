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


/* Order module related interface start */
export const ORDER_PRICE = 'shop/prices';
export const ORDER_CREATE = 'orders';
export const ORDER_PAYMENT = 'orders/:orderId/payment';
export const ORDER_STATUS = 'orders/:orderId/paid';
export const DRY_RUN = 'orders/dryRun/generate';
export const PAID_CHECK = 'orders/:orderId/paidCheck\n';
/* Order module related interface end */