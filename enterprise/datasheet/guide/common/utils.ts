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

export const getStyleProperty = (element: any, propertyName: string, prefixVendor = false): any => {
  if (prefixVendor) {
    const prefixes = ['', '-webkit-', '-ms-', 'moz-', '-o-'];
    for (let counter = 0; counter < prefixes.length; counter++) {
      const prefixedProperty = prefixes[counter] + propertyName;
      const foundValue = getStyleProperty(element, prefixedProperty);

      if (foundValue) {
        return foundValue;
      }
    }

    return '';
  }

  let propertyValue = '';

  if (element.currentStyle) {
    propertyValue = element.currentStyle[propertyName];
  } else if (document.defaultView && document.defaultView.getComputedStyle) {
    propertyValue = document.defaultView
      .getComputedStyle(element, null)
      .getPropertyValue(propertyName);
  }

  return propertyValue && propertyValue.toLowerCase ? propertyValue.toLowerCase() : propertyValue;
};

// Convert object-formatted styles to string format
export const stringifyStyleObject = (styleObj: React.CSSProperties) => {
  const arr: string[] = [];
  Object.keys(styleObj).forEach(key => {
    const formateKey = key.split('').map(i => i.match(/[A-Z]/) ? `-${i.toLocaleLowerCase()}` : i).join('');
    const curStyle = `${formateKey}: ${styleObj[key]}`;
    arr.push(curStyle);
  });
  return arr.join(';');
};

