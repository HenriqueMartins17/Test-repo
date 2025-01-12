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

import dynamic from 'next/dynamic';
import { FC, PropsWithChildren, useState, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import Player from 'xgplayer';
import { Typography, colors } from '@apitable/components';
import { Strings, t, integrateCdnHost } from '@apitable/core';
import { CloseOutlined } from '@apitable/icons';
import { ButtonBase } from 'pc/components/common';
import { Modal as ModalBase } from 'pc/components/common/modal/modal/modal';
import { Loading } from 'pc/components/preview_file/preview_type/preview_doc/loading';
import styles from './modal.module.less';

const PreviewMedia = dynamic(() => import('./media'), {
  loading: () => <Loading />,
  ssr: false,
});

export interface IGuideModalOptions {
  title: string;
  description: string;
  backdrop?: boolean;
  video?: string;
  onClose?: () => void;
  onPlay?: () => void;
  videoId?: string;
  autoPlay?: boolean;
  onDestroy?: () => void;
  onPlayerReady?: (player: Player) => void;
}

export const Modal: FC<PropsWithChildren<IGuideModalOptions>> = (props) => {
  const { title, backdrop, video, onClose, videoId, autoPlay, onPlay } = props;
  const [show, setShow] = useState(true);
  const videoPlayerRef = useRef<Player | null>(null);

  const isYoutubeVideo = video?.includes('youtube');

  const finalClose = () => {
    // setPricingModalOpen(true);
    setShow(false);
    onClose && onClose();
    onPlay && onPlay();

    if (videoId) {
      if (isYoutubeVideo) {
        const iframeTag = document.getElementById(videoId) as HTMLIFrameElement;
        if (iframeTag) {
          const iframeSrc = iframeTag.src;
          iframeTag.src = iframeSrc;
        }
      } else {
        const videoElement = document.getElementById(videoId) as HTMLVideoElement;
        if (videoElement && videoPlayerRef.current) {
          videoPlayerRef.current.pause();
        }
      }
    }
  };

  // const [pricingModalOpen, setPricingModalOpen] = useState(false);

  // useEffect(() => {
  //   if (pricingModalOpen) {
  //     expandUpgradeSpace();
  //   }
  // }, [pricingModalOpen]);

  return (
    <ModalBase
      title=""
      visible={show}
      // onOk={handleOk}
      closable={false}
      maskClosable={!video}
      onCancel={finalClose}
      centered
      mask={backdrop}
      width={'auto'}
      className={styles.modalStyle}
      footer={null}
    >
      <div className={styles.modalHeader}>
        <div />
        <Typography variant="body1">{title}</Typography>
        <div className={styles.modalHeaderClose} onClick={finalClose} id={videoId + '_CLOSE'}>
          <CloseOutlined color={colors.textCommonPrimary} />
        </div>
      </div>
      <div className={styles.modalContent}>
        {video &&
          (!isYoutubeVideo ? (
            <PreviewMedia
              video={`${integrateCdnHost(video)}`}
              autoPlay={autoPlay}
              onPlay={onPlay}
              id={videoId}
              onPlayerReady={(player) => {
                videoPlayerRef.current = player;
              }}
              width={785}
              height={480}
            />
          ) : (
            <iframe
              width="854"
              height="480"
              src={video}
              title="YouTube video player"
              frameBorder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowFullScreen
              className={styles.modalIframe}
              id={videoId}
            />
          ))}
        <div className={styles.modalFooter}>
          <ButtonBase className={styles.modalFooterLeftButton} onClick={finalClose}>
            🚀 {t(Strings.guide_flow_modal_get_started)}
          </ButtonBase>
          <ButtonBase
            className={styles.modalFooterRightButton}
            onClick={() => {
              window.open('https://vika.cn/share/shrlRw3YWmqZ4BMl0B7qZ/fomUMtKMblNchCG7Ef', '_blank');
            }}
          >
            🧭 {t(Strings.guide_flow_modal_contact_sales)}
          </ButtonBase>
        </div>
      </div>
    </ModalBase>
  );
};

export const showModal = (props: PropsWithChildren<IGuideModalOptions>) => {
  const { children, video, ...rest } = props;

  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };

  const render = () => {
    setTimeout(() => {
      const div = document.createElement('div');
      div.setAttribute('class', video ? 'vika-guide-modal vika-guide-modal-video' : 'vika-guide-modal');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        <Modal video={video} onDestroy={destroy} {...rest}>
          {children}
        </Modal>,
      );
    });
  };

  const run = () => {
    destroy();
    render();
  };

  run();
};

export const destroyModal = () => {
  const destroy = () => {
    const dom = document.querySelector('.vika-guide-modal');
    dom && document.body.removeChild(dom);
  };

  destroy();
};
