import { Typography } from '@apitable/components';
import styles from './index.module.less';
interface IProps {
  content: string;
  logo: React.ReactNode;
  title: string;
}

export function Banner(props: IProps) {
  return (
    <div className={styles.banner}>
      { props.logo }
      <div className={styles.content}>
        <div className={styles.title}>
          <Typography variant={'h3'}>{ props.title }</Typography>
          <span className={styles.beta}>
            <Typography style={{
              color: 'var(--rainbowPurple5)'
            }} variant={'h9'}>BETA</Typography>
          </span>
        </div>
        <div className={styles.desc}>{ props.content }</div>
      </div>
    </div>
  );
}
