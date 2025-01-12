import { useState } from 'react';
import { useSelector } from 'react-redux';
import { CodeLanguage } from 'pc/components/api_panel/field_codes';
import githubIcon from 'static/icon/common/github_octopus.png';
import { DocInner } from '../components/doc_inner/doc_inner';
import { LanguageToggleBtn } from '../components/language_toggle_btn/language_toggle_btn';
import styles from './style.module.less';

interface IApiDocProps {
  token: string
}
const API_BASE = 'https://api.vika.cn';

export const ApiDoc: React.FC<IApiDocProps> = ({ token }) => {
  const [language, setLanguage] = useState(CodeLanguage.Python);
  const { aiId } = useSelector(state => state.pageParams);

  const tempateConfig = {
    token,
    aiId: aiId!,
    apiBase: window.location.origin.includes('vika.cn') ? API_BASE : window.location.origin,
    githubIcon: githubIcon.src,
  };

  return <div className={styles.apiDoc} >
    <LanguageToggleBtn setLanguage={setLanguage} language={language} />
    <DocInner language={language} tempateConfig={tempateConfig} />
  </div>;
};
