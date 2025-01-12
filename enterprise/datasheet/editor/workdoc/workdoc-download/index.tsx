import { Dropdown, Menu } from 'antd';
import FileSaver from 'file-saver';
import React from 'react';
import { createPortal } from 'react-dom';
import { IconButton } from '@apitable/components';
import { DownloadOutlined } from '@apitable/icons';
import { IDownloadDocProps } from '../../editor_core/editor-core/interface';
import { html2mk } from './html2mk';
import styles from './styles.module.less';

export const WorkdocDownload = (props: IDownloadDocProps) => {
  const { editor, documentId } = props;

  if (!editor) return null;

  //   const handleDownloadHtml = () => {
  //     const htmlOutput = editor.getHTML().replace('image-resizer', 'img');
  //     const htmlWithStyles = `
  //   <html>
  //     <head>
  //       ${htmlStyles}
  //     </head>
  //     <body>
  //       <div id="content" class="ProseMirror">
  //         ${htmlOutput}
  //       </div>
  // </div>
  //     </body>
  //   </html>
  // `;
  //     FileSaver.saveAs(new Blob([htmlWithStyles], { type: 'text/html;charset=utf-8' }), `${documentId}.html`);
  //   };

  const handleDownloadMarkdown = () => {
    const htmlOutput = editor.getHTML().replace('image-resizer', 'img');
    const markdownOutput = html2mk(htmlOutput);
    FileSaver.saveAs(new Blob([markdownOutput], { type: 'text/markdown;charset=utf-8' }), `${documentId}.md`);
  };

  const dropdownMenu = (
    <Menu>
      {/*<Menu.Item key="downloadHtml" onClick={handleDownloadHtml}>*/}
      {/*  HTML*/}
      {/*</Menu.Item>*/}
      <Menu.Item key="downloadMarkdown" onClick={handleDownloadMarkdown}>
        Markdown
      </Menu.Item>
    </Menu>
  );

  const nodeId = document.getElementById('workdocDownload');
  if (!nodeId) return null;

  return createPortal(
    <Dropdown overlay={dropdownMenu}>
      <div className={styles.downloadIconButton}>
        <IconButton icon={DownloadOutlined} shape="square" />
      </div>
    </Dropdown>,
    nodeId
  );
};
