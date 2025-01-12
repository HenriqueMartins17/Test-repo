import Taro, { } from '@tarojs/taro';
import { Text } from '@tarojs/components';
import './index.less';

interface IT {
  cc: string;
}

export default function AuthPage({ cc }: IT) {
  // return <Text className={'tt'}>Hello, {cc}</Text>;
  console.log('123');
  return <Text className={'tt'}>Hello,123,{cc}</Text>;
}

AuthPage.config = {
  navigationBarTitleText: '首1页',
};

AuthPage.defaultProps = {
  cc: 'ccc',
};

