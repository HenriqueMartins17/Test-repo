import Taro, { useRouter } from '@tarojs/taro';
import { View, Image } from '@tarojs/components';
// import { Api } from '../../api';
import classnames from 'classnames';
import iconFail from '@static/icon/common_icon_warning.svg';
import styles from './index.module.less';

const FailPage = () => {
  const { params } = useRouter();
  const desc = params.type === '0' ? '小程序码已失效' : '您已取消此次绑定操作';
  const subDesc = params.type === '0' ? '请在电脑端刷新后重新扫码绑定' : '可再次扫码进行帐号绑定';

  return (
    <View className={classnames('container', styles.success)}>
      <Image
        className="image"
        src={iconFail}
      />
      <View className={styles.successTitle}>{desc}</View>
      <View className={styles.successDesc}>{subDesc}</View>
    </View>
  );
};

FailPage.config = {
  navigationBarTitleText: '维格数表',
};

export default FailPage;
