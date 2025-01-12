import Taro, { Component, useEffect } from '@tarojs/taro';
import { View, Text } from '@tarojs/components';
import './index.less';

interface IT {
  cc: string;
}

export default function AuthPage({ cc }: IT) {
  // return <Text className={'tt'}>Hello, {cc}</Text>;
  useEffect(() => {
    console.log(cc);
   }, []);
  return <Text className={'tt'}>Hello,123,{cc}</Text>;
}

AuthPage.config = {
  navigationBarTitleText: '首1页',
};

AuthPage.defaultProps = {
  cc: 'ccc',
};

