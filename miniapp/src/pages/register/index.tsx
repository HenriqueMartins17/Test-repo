import { Api } from '@api';
import { colors } from '@styles/color';
import { Button, Input, Radio, Text, View } from '@tarojs/components';
import Taro, { useRef, useState } from '@tarojs/taro';
import classnames from 'classnames';
import styles from './index.module.less';

const Register = props => {
  const [phoneType, setPhoneType] = useState('');
  const [codeType, setCodeType] = useState('');
  const [count, setCount] = useState({
    count: 60,
    desc: '获取验证码',
  });
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const globalRef = useRef<{ timer: number | null }>({ timer: null });
  const [check, setCheck] = useState(false);

  const onFocus = (type: 'phone' | 'code') => {
    if (type === 'phone') {
      return setPhoneType('focus');
    }
    return setCodeType('focus');
  };

  const onBlur = (type: 'phone' | 'code') => {
    if (type === 'phone') {
      if (phone.length === 0) return setPhoneType('');
      if (phone.length !== 11) {
        return setPhoneType('error');
      }
      const pattern = RegExp('1[3456789]\\d{9}');
      if (!pattern.test(phone)) {
        setPhoneType('error');
      } else {
        setPhoneType('');
      }
    } else {
      if (code.length === 0) return setCodeType('');
      if (code.length !== 6) {
        return setCodeType('error');
      }
      setCodeType('');
    }

  };

  const onInput = (type: 'phone' | 'code', value) => {
    if (type === 'phone') {
      setPhone(value);
    } else {
      setCode(value);
    }
  };


  const sendCode = () => {
    if (globalRef.current.timer != null) {
      return;
    }
    if (phone.length !== 11) {
      return setPhoneType('error');
    }
    setCount({
      count: 59,
      desc: 's 后重发',
    });
    Api.getCmsCode(phone, 2);
    globalRef.current!.timer = setInterval(() => {
      setCount(count => {
        if (count.count === 1) {
          clearInterval(globalRef.current.timer!);
          globalRef.current.timer = null;
          return {
            count: 60,
            desc: '重新获取',
          };
        }
        return {
          count: count.count - 1,
          desc: 's 后重发',
        };
      });
    }, 1000);
  };

  function toggleRadio() {
    setCheck(check => !check);
  }

  const loginIn = () => {

    Api.getLogin({
      username: phone,
      credential: code,
      type: 'wechat_sms_code',
    }).then(res => {
      // 原来的逻辑，登录以后进入成功页面
      // let type;
      // if (res.isBind && !res.needCreate) {
      //   type = 'oldUser';
      // }
      // if (!res.isBind) {
      //   type = 'newUser';
      // }
      // if (res.isBind && res.needCreate) {
      //   type = 'newUser';
      // }
      // Taro.navigateTo({
      //   url: '../success_page/index?type=' + type,
      // });

      // v0.3 返回我的页面
      Taro.switchTab({
        url: '../my_info/index',
      });
    }).catch(err => {
      Taro.showToast({
        title: err.message,
        icon: 'none',
        duration: 2000,
      });
      if (err.code === 231) {
        setCodeType('error');
      }
      if (err.code === 303) {
        setPhoneType('error');
      }
    });
  };

  const canLogin = phone.length === 11 && phoneType === '' && code.length === 6 && check;

  return (
    <View className={classnames('container', styles.register)}>
      <Text className={styles.h1}>
        验证码登录
      </Text>
      <View className={classnames(styles.registerPhone, styles[phoneType])}>
        <View className={styles.registerPhoneCode}>
          +86
        </View>
        <View className={styles.registerPhoneInput}>
          <Input
            type="number"
            placeholder-class="phcolor"
            maxLength={11}
            onInput={({ detail }) => { onInput('phone', detail.value); }}
            onFocus={() => { onFocus('phone'); }}
            onBlur={() => { onBlur('phone'); }}
            placeholder="请输入手机号"
          />
        </View>
      </View>

      <View className={classnames(styles.registerCode, styles[codeType])}>

        <View className={styles.registerCodeInput}>
          <Input
            type="number"
            placeholder-class="phcolor"
            maxLength={6}
            onInput={({ detail }) => { onInput('code', detail.value); }}
            onFocus={() => { onFocus('code'); }}
            onBlur={() => { onBlur('code'); }}
            placeholder="请输入验证码"
          />
        </View>
        <View className={styles.registerCodeButton} onClick={sendCode}>
          {
            count.count === 60 ? count.desc : count.count + count.desc
          }
        </View>


      </View>

      <View onClick={toggleRadio} className={styles.deal}>
        <Radio style="transform:scale(0.7);" color={colors.fc0} value="" checked={check} />
        我已阅读并同意<Text className={styles.blue}>《用户协议》</Text>与<Text className={styles.blue}>《隐私权协议》</Text>
      </View>
      <Button
        className={styles.login}
        onClick={() => { canLogin && loginIn(); }}
        style={{
          opacity: canLogin ? 1 : 0.5,
        }}
      >
        登录
      </Button>
    </View>
  );
};

Register.config = {
  navigationBarTitleText: '',
};

export default Register;
