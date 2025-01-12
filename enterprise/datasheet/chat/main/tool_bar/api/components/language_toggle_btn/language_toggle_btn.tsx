import classNames from 'classnames';
import { LinkButton } from '@apitable/components';
import { CodeLanguage } from 'pc/components/api_panel/field_codes';
import styles from './style.module.less';

interface ILanguageToggleBtnProps {
  setLanguage: React.Dispatch<React.SetStateAction<CodeLanguage>>
  language:CodeLanguage
}

export const LanguageToggleBtn:React.FC<ILanguageToggleBtnProps> = ({ setLanguage, language }) => {
  return <div className={classNames('vk-flex vk-justify-end', styles.radioGroup)}>
    <LinkButton
      underline={false}
      component="button"
      className={classNames({ [styles.radioActive]: CodeLanguage.Python === language })}
      onClick={() => setLanguage(CodeLanguage.Python)}
    >
      Python
    </LinkButton>
    <LinkButton
      underline={false}
      component="button"
      className={classNames({ [styles.radioActive]: CodeLanguage.Js === language })}
      onClick={() => setLanguage(CodeLanguage.Js)}
    >
      JavaScript
    </LinkButton>
    <LinkButton
      underline={false}
      component="button"
      className={classNames({ [styles.radioActive]: CodeLanguage.Curl === language })}
      onClick={() => setLanguage(CodeLanguage.Curl)}
    >
      cURL
    </LinkButton>
  </div>;
};
