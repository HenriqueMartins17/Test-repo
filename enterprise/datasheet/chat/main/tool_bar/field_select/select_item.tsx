import { Col, Row } from 'antd';
import { Switch } from '@apitable/components';
import { ISelectItemProps } from './interface';
import styles from './style.module.less';

export const SelectItem: React.FC<ISelectItemProps> = ({
  checked = false, label, prefixIcon,
}) => {
  return <Row className={styles.selectItem} align={'middle'}>
    {
      prefixIcon && <Col className={styles.iconWrapper}>
        {prefixIcon}
      </Col>
    }
    <Col flex={1} className={styles.label}>
      {label}
    </Col>
    <Col>
      <Switch checked={checked} size={'small'}/>
    </Col>

  </Row>;
};
