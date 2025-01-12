/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { Object } from 'ts-toolbelt';
import { ThemeName } from '@apitable/components';
import { IWizardsConfig } from '@apitable/core';
import { IUserProfile } from 'api/user';
import { getEnvVars } from 'get_env';

const envVars = getEnvVars();
type IEnvVars = Object.Update<typeof envVars, 'THEME', ThemeName | undefined>;

export interface IInitializationData {
  userInfo?: IUserProfile;
  version?: string;
  wizards?: IWizardsConfig;
  env: string;
  locale: string;
  lang?: string;
  envVars: IEnvVars;
}

declare global {
  // eslint-disable-next-line @typescript-eslint/naming-convention
  interface Window {
    __initialization_data__: IInitializationData;
  }

  // eslint-disable-next-line @typescript-eslint/naming-convention
  interface ResizeObserver {
    observe(target: Element): void;
    unobserve(target: Element): void;
    disconnect(): void;
  }

  const ResizeObserver: {
    prototype: ResizeObserver;
    new (callback: ResizeObserverCallback): ResizeObserver;
  };

  // eslint-disable-next-line @typescript-eslint/naming-convention
  interface ResizeObserverSize {
    inlineSize: number;
    blockSize: number;
  }

  type ResizeObserverCallback = (entries: ReadonlyArray<ResizeObserverEntry>, observer: ResizeObserver) => void;

  // eslint-disable-next-line @typescript-eslint/naming-convention
  interface ResizeObserverEntry {
    readonly target: Element;
    readonly contentRect: DOMRectReadOnly;
    readonly borderBoxSize: ResizeObserverSize;
    readonly contentBoxSize: ResizeObserverSize;
  }
}
