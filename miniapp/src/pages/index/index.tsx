import { WebView } from '@tarojs/components';
import Taro, { useEffect, useRouter } from '@tarojs/taro';
import { Api } from '../../api';
import { indexUrl } from '../../config/index';

const Index = () => {
  const router = useRouter();
  useEffect(() => {
    Taro.clearStorageSync();
    let scene = router.params.scene;
    if (scene) {
      scene = decodeURIComponent(scene as string);
      const queryArr = scene.split('&');
      const query = {
        type: '',
        mark: '',
      };
      queryArr.forEach(function (item: string) {
        const value = item.split('=')[1];
        const key = item.split('=')[0];
        query[key] = value;
      });
      Api.qrCodeOperate(query.mark, 0).then(res => {
        if (query.type === '0') {
          // web端扫码登录
          Taro.navigateTo({
            url: `/pages/auth/index?mark=${query.mark}`,
          });
        }
        if (query.type === '1') {
          // 微信号绑定
          Taro.navigateTo({
            url: `/pages/bind_account/index?mark=${query.mark}`,
          });
        }
      }).catch(res => {
        Taro.navigateTo({
          url: '/pages/fail_page/index?type=0',
        });
      });

    }
  }, []);

  return (
    <WebView src={indexUrl} />
    // <WebView src="https://vikadata.com" />
  );
};

Index.config = {
  navigationBarTitleText: '首页',
};

export default Index;
