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

import { useMount, useUpdateEffect, useUnmount } from 'ahooks';
import { useRef, useEffect } from 'react';
import * as React from 'react';
import Player from 'xgplayer';
import { t, Strings } from '@apitable/core';

interface IPreviewTypeBase {
  video: string;
  autoPlay?: boolean;
  onPlay?: () => void;
  id?: string;
  poster?: string;
  width?: number;
  height?: number;
}

interface IPreviewTypeExtra {
  onPlayerReady?: (player: Player) => void
}
const PreviewMedia: React.FC<IPreviewTypeBase & IPreviewTypeExtra> = (props) => {
  const { autoPlay, video, onPlay, id, poster, width, height } = props;
  const ref = useRef<HTMLDivElement>(null);
  const playerRef = useRef<Player>();
  const canAutoPlay = useRef<boolean>(true);

  function closeAutoPlay() {
    return (canAutoPlay.current = false);
  }

  useMount(() => {
    const playerOptions = {
      closeVideoStopPropagation: false,
      el: ref.current!,
      fluid: !(width && height),
      url: `${video}`,
      videoInit: true,
      errorTips: t(Strings.video_not_support_play),
      playsinline: true,
      poster,
      ...(width && { width }),
      ...(height && { height }),
    };

    playerRef.current = new Player(playerOptions);
    props.onPlayerReady?.(playerRef.current);

    if (autoPlay) {
      playerRef.current.play();
      onPlay && onPlay();
      closeAutoPlay();
    }
  });

  useEffect(() => {
    const onMouseup = () => {
      setTimeout(() => {
        playerRef.current!.hasStart && onPlay && onPlay();
      }, 200);
    };
    const dom = ref.current;
    dom?.addEventListener('mouseup', onMouseup);
    return () => {
      dom?.removeEventListener('mouseup', onMouseup);
    };
  }, [onPlay]);
  useUnmount(() => {
    playerRef.current?.destroy();
  });

  useUpdateEffect(() => {
    if (!playerRef.current) {
      return;
    }

    if (!autoPlay) {
      return playerRef.current.pause();
    }

    if (canAutoPlay.current && autoPlay) {
      playerRef.current.play();
      onPlay && onPlay();
      closeAutoPlay();
    }
  }, [autoPlay]);

  return <div ref={ref} id={id} />;
};

export default PreviewMedia;
