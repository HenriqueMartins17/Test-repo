import { DrawerWrapper } from '@apitable/ai';
import { ApiPanel } from './api_panel/api_panel';
import styles from './style.module.less';

interface IApiWrapperProps {
  visible: boolean;
  close: () => void;
}

export const ApiWrapper: React.FC<IApiWrapperProps> = ({ visible, close }) => {
  return <DrawerWrapper
    open={visible}
    close={close}
    classNames={styles.wrapper}
    config={{
      title: null,
      modalWidth: 1000,
    }}

  >
    <ApiPanel close={close} />
  </DrawerWrapper>;
};
