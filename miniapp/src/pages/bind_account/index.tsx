import { Api } from '@api';
import { colors } from '@styles/color';
import { Button, OpenData, View } from '@tarojs/components';
import Taro, { useRouter，useEffect } from '@tarojs/taro';
import classnames from 'classnames';
import styles from './index.module.less';


const BindAccount = () => {
  // bindWechatOperate
  const { params } = useRouter();
  const operate = type => {
    Api.qrCodeOperate(params.mark, type).then(() => {
      if (type === 2) {
        Taro.redirectTo({
          url: '/pages/fail_page/index',
        });
      }
      if (type === 3) {
        Taro.redirectTo({
          url: '/pages/success_page/index?type=' + 'oldUser' + '&isBind=true',
        });
      }
    }).catch(res => {
      Taro.showModal({
        title: '操作失败',
        content: res.message,
        showCancel: false,
        confirmColor: colors.fc0,
      });
    });
  };

  useEffect(() => {
    Taro.login({
      success(res: any) {
        if (res.code) {
          Api.bindWechatCode(res.code);
        } else {
          console.log('登录失败！' + res.errMsg);
        }
      },
    });
  });


  return (
    <View className={classnames('container', styles.bindAccount)}>
      <View className={styles.avatar}>
        <OpenData type="userAvatarUrl" />
      </View>
      <View className={styles.nackName}>
        <OpenData type="userNickName" />
      </View>
      <View className={styles.desc}>
        将使用当前微信帐号与维格数表绑定
      </View>
      <Button
        type="primary"
        style={{ background: colors.fc0, borderRadius: '25px' }}
        onClick={() => { operate(3); }}
      >
        绑定微信
      </Button>
      <Button plain style={{ border: 'none', marginTop: '32px' }} onClick={() => { operate(2); }}>取消</Button>
    </View>
  );
};


export default BindAccount;
