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

import * as React from 'react';
import { colors, getThemeColors, getThemeName, lightColors, ThemeName, convertHexToRGB } from '@apitable/components';
import { CutMethod, getImageThumbSrc, integrateCdnHost } from '@apitable/core';
import { AvatarBase, IAvatarBaseProps } from './avatar_base';

const createRainbowColorArrByShade = (baseHueArr: any[], shade: number) => {
  return baseHueArr.map((hue) => hue[shade]);
};

const createRainbowColorArrByOpacity = (baseHueArr: any[], opacity: number) => {
  const yellowIndex = 5;
  return baseHueArr.map((hue, index) =>
    // Deal with yellow background and white font problem
    convertHexToRGB(hue[400], index === yellowIndex && opacity === 1 ? 0.85 : opacity),
  );
};

function createAvatarRainbowColorsArr(theme: ThemeName) {
  const isLightTheme = theme === ThemeName.Light;
  const { deepPurple, indigo, blue, teal, green, yellow, orange, tangerine, pink, red } = colors;
  const baseHueArr = [deepPurple, indigo, blue, teal, green, yellow, orange, tangerine, pink, red];

  return isLightTheme ? createRainbowColorArrByShade(baseHueArr, 500) : createRainbowColorArrByOpacity(baseHueArr, 1);
}

export enum AvatarSize {
  Size16 = 16,
  Size20 = 20,
  Size24 = 24,
  Size32 = 32,
  Size40 = 40,
  Size56 = 56,
  Size64 = 64,
  Size80 = 80,
  Size120 = 120,
}

const ColorWheel = Object.keys(lightColors).reduce<string[]>((pre, cur) => {
  if (cur.startsWith('rc')) {
    pre.push(lightColors[cur]);
  }
  return pre;
}, []);

export const getAvatarRandomColor = (str: string) => {
  const index = (str + '').charCodeAt(Math.floor(str.length / 2));
  return ColorWheel[index % ColorWheel.length];
};

export function getFirstWordFromString(str: string) {
  if (!str) return '';
  const word = str.trim();
  if (!word.length) return '';
  const codePoint = word.codePointAt(0);
  if (!codePoint) return '';
  return String.fromCodePoint(codePoint).toUpperCase();
}

export enum AvatarType {
  Member,
  Space,
  Team,
}

export interface IAvatarProps extends Omit<IAvatarBaseProps, 'shape'> {
  id?: string;
  title: string;
  isGzip?: boolean;
  children?: JSX.Element;
  type?: AvatarType;
  avatarColor?: number | null;
  style?: React.CSSProperties;
  defaultIcon?: JSX.Element;
  isRole?: boolean;
}

const AvatarHoc = (Component: any) => {
  const ratio = process.env.SSR ? 2 : Math.max(window.devicePixelRatio, 2);
  const colors = getThemeColors();
  const themeName = getThemeName();
  const bgColorList = createAvatarRainbowColorsArr(themeName);

  const FC = (props: IAvatarProps) => {
    const { src, title, isGzip = true, id = 'none', size = AvatarSize.Size32, style, defaultIcon, avatarColor } = props;

    if (!title) return null;

    const avatarSrc =
      isGzip && src
        ? getImageThumbSrc(integrateCdnHost(src), {
          method: CutMethod.CUT,
          quality: 100,
          size: size * ratio,
        })
        : src;
    const firstWord = getFirstWordFromString(title);
    const avatarBg = avatarSrc ? colors.defaultBg : avatarColor != null ? bgColorList[avatarColor] : getAvatarRandomColor(id);

    return (
      <Component
        {...props}
        shape="circle"
        src={avatarSrc}
        style={{
          backgroundColor: defaultIcon ? colors.rc01 : avatarBg,
          color: colors.textStaticPrimary,
          border: !src && '0px',
          ...style,
        }}
      >
        {!avatarSrc && (defaultIcon || firstWord)}
      </Component>
    );
  };

  return FC;
};

export const Avatar = AvatarHoc(AvatarBase);
