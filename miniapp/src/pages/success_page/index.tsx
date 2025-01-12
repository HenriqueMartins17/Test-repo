import Taro, { useRouter } from '@tarojs/taro';
import { View, Image } from '@tarojs/components';
// import { Api } from '../../api';
import classnames from 'classnames';
import iconLogo from '@static/icon/common_icon_success.svg';
import styles from './index.module.less';

const SuccessPage = () => {
  const { params } = useRouter();
  const desc = params.type === 'oldUser' ? '欢迎使用维格数表，请在PC端打开维格表' : '欢迎使用维格数表，请在PC端完成初始操作';
  return (
    <View className={classnames('container', styles.success)}>
      <Image
        className="image"
        src={iconLogo}
      />
      <View className={styles.successTitle}>{params.isBind ? '绑定成功' : '登录成功'}</View>
      <View className={styles.successDesc}>{desc}</View>
    </View>
  );
};

SuccessPage.config = {
  navigationBarTitleText: '维格数表',
};

export default SuccessPage;
