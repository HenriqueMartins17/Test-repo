import iconLogo from '@static/icon/logo.png';
import iconName from '@static/icon/vikadata.png';
import { colors } from '@styles/color';
import { Button, Image, View, Navigator } from '@tarojs/components';
import Taro, { useEffect, useState, useRouter } from '@tarojs/taro';
import { Api } from '../../api';
import styles from './index.module.less';

export default function AuthPage() {
  const [isBinding, setIsBinding] = useState<boolean>(false);
  const [needCreate, setNeedCreate] = useState<boolean>(false);
  const { params } = useRouter();
  const isWebLogin = params.mark ? true : false;

  async function cbForGetPhone(...rest: any[]) {
    if (rest[0].detail.errMsg !== 'getPhoneNumber:ok') return;
    await Api.bindUserPhone({
      encryptedData: rest[0].detail.encryptedData,
      iv: rest[0].detail.iv,
    }).catch(error => {
      Taro.showModal({
        title: '操作失败',
        content: '该手机号已被其他微信帐号绑定，请使用其他方式进行登录。',
        showCancel: false,
        confirmColor: colors.fc0,
      });
    });
    if (!params.mark) return;
    await Api.qrCodeOperate(params.mark, 1);
    let type;
    if (isBinding && !needCreate) {
      type = 'oldUser';
    }
    if (isBinding && needCreate) {
      type = 'newUser';
    }
    Taro.redirectTo({
      url: '../success_page/index?type=' + type,
    });
  }

  useEffect(() => {
    Taro.login({
      success(res: any) {
        if (res.code) {
          Api.bindWechatCode(res.code).then(res => {
            if (res.isBind) {
              setIsBinding(true);
            }
            if (res.needCreate) {
              setNeedCreate(true);
            }
          });
        } else {
          console.log('登录失败！' + res.errMsg);
        }
      },
    });
  }, []);

  const toLogin = () => {
    Api.qrCodeOperate(params.mark, 1).then(() => {
      let type;
      if (isBinding && !needCreate) {
        type = 'oldUser';
      }
      if (isBinding && needCreate) {
        type = 'newUser';
      }
      Taro.redirectTo({
        url: '../success_page/index?type=' + type,
      });
    });
  };

  const toRegister = () => {
    Taro.navigateTo({
      url: '../register/index',
    });
  };

  function cancelLogin() {
    Api.qrCodeOperate(params.mark, 2);
  }

  return (
    <View className={'container'}>
      <View className={styles.imgContainer}>
        <Image
          className={styles.image}
          src={iconLogo}
        />
      </View>

      <View className={styles.h1}>
        {/* <Text>vikadata</Text> */}
        <Image
          className={styles.image}
          src={iconName}
        />
      </View>
      {
        isBinding ?
          (
            <Button
              type="primary"
              style={{ backgroundColor: colors.fc0 }}
              onClick={toLogin}
            >
              {isWebLogin ? '微信授权' : '微信登录'}
            </Button>
          ) :
          (
            <Button
              type="primary"
              style={{ backgroundColor: colors.fc0 }}
              open-type="getPhoneNumber"
              onGetPhoneNumber={cbForGetPhone}
            >
              {isWebLogin ? '微信授权' : '微信登录'}
            </Button>
          )
      }
      {
        isWebLogin ?
          (
            <Navigator
              style={{ border: 'none', marginTop: '32px', textAlign: 'center' }}
              open-type="exit"
              target="miniProgram"
              onTouchStart={cancelLogin}
            >
              取消
            </Navigator>
          ) :
          (<Button plain style={{ borderColor: '#DADCE5', marginTop: '32px' }} onClick={toRegister}>手机验证码快捷登录</Button>)
      }
    </View >
  );
}

AuthPage.config = {
  navigationBarTitleText: '维格数表',
};
