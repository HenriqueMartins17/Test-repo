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

import { useEffect, useImperativeHandle, useState } from 'react';
import * as React from 'react';

export interface IScriptProps {
  src: string;
  reloadCount?: number;
  onload?: () => void;
  onerror?: () => void;
}

export interface IScriptRef {
  reload(cb: any): any;
}

const ScriptBase: React.ForwardRefRenderFunction<IScriptRef, IScriptProps> = (props, ref) => {

  const { src, onload, onerror, reloadCount = 3 } = props;
  const [count, setCount] = useState(0);

  useImperativeHandle(ref, (): IScriptRef => {
    const reload = (cb: () => any) => {
      if (count >= reloadCount) {
        return cb();
      }
      const cur = document.getElementById(src);
      if (cur) {
        document.body.removeChild(cur);
      }
      setCount(count + 1);
    };
    return {
      reload
    };
  });
  
  useEffect(() => {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = src;
    script.setAttribute('id', src);
    document.body.appendChild(script);
    script.onload = onload || (() => {});
    script.onerror = () => {
      onerror && onerror();
    };
    return () => {
      script && document.body.contains(script) && document.body.removeChild(script);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [count]);
  return <></>;
};

export const Script = React.forwardRef(ScriptBase);