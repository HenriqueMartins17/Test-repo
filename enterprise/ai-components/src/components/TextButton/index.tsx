import cls from 'classnames';
import { ITextButtonProps, TextButton as TextButtonComponents } from '@apitable/components';
import styles from './style.module.less';

interface IProps extends ITextButtonProps {}

export function TextButton(props: IProps) {
  const className = cls(props.className, styles.item);
  return <TextButtonComponents {...props} className={className} />;
}
