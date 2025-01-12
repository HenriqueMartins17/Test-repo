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

export interface IExtra {
  // For usage-based functions, pass in how many quantities are currently in use
  usage?: number;
  // For overages, should obstructive prompts
  alwaysAlert?: boolean;
  // The function currently checked and to which subscription level it belongs
  grade?: string;
  // Do not use template information, use this when you want to customize
  message?: string;
  reload?: boolean;
}
