import Taro, { Component, Config } from '@tarojs/taro';
import { Provider } from '@tarojs/redux';

import Index from './pages/index';

import configStore from './store';

import './app.less';

// 如果需要在 h5 环境中开启 React Devtools
// 取消以下注释：
// declare function require(name: string);
// if (process.env.NODE_ENV !== 'production' && process.env.TARO_ENV === 'h5')  {
//   require('nerv-devtools')
// }

const store = configStore();

class App extends Component {

  /**
   * 指定config的类型声明为: Taro.Config
   *
   * 由于 typescript 对于 object 类型推导只能推出 Key 的基本类型
   * 对于像 navigationBarTextStyle: 'black' 这样的推导出的类型是 string
   * 提示和声明 navigationBarTextStyle: 'black' | 'white' 类型冲突, 需要显示声明类型
   */
  // eslint-disable-next-line react/sort-comp
  config: Config = {
    pages: [
      'pages/index/index',
      'pages/auth/index',
      'pages/fail_page/index',
      'pages/bind_account/index',
      'pages/register/index',
      'pages/success_page/index',
      'pages/my_info/index',
    ],
    window: {
      backgroundTextStyle: 'light',
      navigationBarBackgroundColor: '#fff',
      navigationBarTitleText: 'WeChat',
      navigationBarTextStyle: 'black',
    },
    tabBar: {
      selectedColor: '#2b68ff',
      list: [
        {
          pagePath: 'pages/index/index',
          text: '',
          iconPath: 'page/../static/icon/tabbar_icon_home_normal@3x.png',
          selectedIconPath: 'page/../static/icon/tabbar_icon_home_press@3x.png',
        },
        {
          pagePath: 'pages/my_info/index',
          text: '',
          iconPath: 'page/../static/icon/tabbar_icon_me_normal@3x.png',
          selectedIconPath: 'page/../static/icon/tabbar_icon_me_press@3x.png',
        },
      ],
    },
  };

  componentDidMount() {
    return;
  }

  componentDidShow() { return; }

  componentDidHide() { return; }

  componentDidCatchError() { return; }

  // 在 App 类中的 render() 函数没有实际作用
  // 请勿修改此函数
  render() {
    return (
      <Provider store={store}>
        <Index />
      </Provider>
    );
  }
}

Taro.render(<App />, document.getElementById('app'));
