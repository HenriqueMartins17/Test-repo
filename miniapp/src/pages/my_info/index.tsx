import { Image, OpenData, Text, View, Button } from '@tarojs/components';
import Taro, { useEffect, useState } from '@tarojs/taro';
import moment from 'moment';
import { Api } from '../../api';
import styles from './index.module.less';
import iconAvatar from '@static/icon/me_img_member.png';
import iconSpace from '@static/icon/me_img_space.png';
import { colors } from '@styles/color';

interface IUseInfo {
  name: string;
  avatar: typeof iconAvatar;
  phone: number | string;
  email: string;
}

interface ISpaceInfo {
  spaceName: string;
  spaceLogo: typeof iconSpace;
  createTime: string;
  creator: string;
  admin: string;
  memberNumber: string;
  departmentNumber: string;
  sheetNumber: string;
  recordNumber: string;
  attachmentSize: string;
}

const defaultUserInfo: IUseInfo = {
  name: '-',
  avatar: iconAvatar,
  phone: '-',
  email: '-',
};

const defaultSpaceInfo: ISpaceInfo = {
  spaceName: '-',
  spaceLogo: iconSpace,
  createTime: '-',
  creator: '-',
  admin: '-',
  memberNumber: '- 人',
  departmentNumber: '- 个',
  sheetNumber: '- 张',
  recordNumber: '- 条',
  attachmentSize: '-',
};

// TODO: 缺少我的页面的logo和空间站logo
export default function AuthPage() {
  const [isLogin, setLogin] = useState(false);
  const [spaceInfo, setSpaceInfo] = useState<ISpaceInfo>(defaultSpaceInfo);
  const [userInfo, setUserInfo] = useState<IUseInfo>(defaultUserInfo);

  useEffect(() => {
    if (isLogin) return;
    Taro.login({
      success(res: any) {
        if (res.code) {
          Api.bindWechatCode(res.code).then(res => {
            if (res.isBind) {
              setLogin(true);
            }
            // if (res.needCreate) {
            // }
          });
        } else {
          console.log('登录失败！' + res.errMsg);
        }
      },
    });
  }, [isLogin]);

  function byte2Mb(byte: number | undefined) {
    if (byte === 0) return 0 + 'M';
    if (byte == null) return 0 + 'M';
    return (byte / 1024 / 1024).toFixed(2) + 'M';
  }

  const getUserInfo = () => {
    Api.getVikaUserInfo().then(res => {
      setUserInfo(state => {
        return {
          ...state,
          name: res.nickName ? res.nickName : state.name,
          avatar: res.avatar ? res.avatar : state.avatar,
          phone: res.mobile ? res.mobile : state.phone,
          email: res.email ? res.email : state.email,
        };
      });
      setSpaceInfo(state => {
        return {
          ...state,
          spaceName: res.spaceName ? res.spaceName : state.spaceName,
          spaceLogo: res.spaceLogo || state.spaceLogo,
          createTime: res.createTime ? moment(res.createTime).format('YYYY-MM-DD') : state.createTime,
          creator: res.creatorName ? res.creatorName : state.creator,
          admin: res.ownerName ? res.ownerName : state.admin,
          memberNumber: res.memberNumber + '人',
          departmentNumber: res.teamNumber + '个',
          sheetNumber: res.fileNumber + '张',
          recordNumber: res.recordNumber + '条',
          attachmentSize: byte2Mb(res.usedSpace) + '/' + byte2Mb(res.maxMemory),
        };
      });
    });
  };

  useEffect(() => {
    if (!isLogin) return;
    getUserInfo();
  }, [isLogin]);

  async function cbForGetPhone(...rest: any[]) {
    if (rest[0].detail.errMsg !== 'getPhoneNumber:ok') return;
    await Api.bindUserPhone({
      encryptedData: rest[0].detail.encryptedData,
      iv: rest[0].detail.iv,
    }).then(() => {
      setLogin(true);
    }).catch(() => {
      Taro.showModal({
        title: '操作失败',
        content: '该手机号已被其他微信帐号绑定，可先前往web端进行解绑操作。',
        showCancel: false,
        confirmColor: colors.fc0,
      });
    });
  }

  return (
    <View className={styles.myInfo}>
      <View className={styles.info} >
        <View className={styles.avator}>
          <Image src={userInfo.avatar} />
        </View>
        {
          isLogin ?
            (
              <View className={styles.name} >
                {userInfo.name}
              </View>
            ) :
            (
              <Button
                style={{ background: 'none', padding: 0, border: 'none' }}
                open-type="getPhoneNumber"
                onGetPhoneNumber={cbForGetPhone}
              >
                点击登录
              </Button>
            )
        }

      </View>
      <View className={styles.listContainer}>
        <View className={styles.selfInfo}>
          <View className={styles.grayTitle}>
            个人信息
          </View>
          <View className={styles.listItem}>
            <View>手机</View>
            <View>{userInfo.phone}</View>
          </View>
          <View className={styles.listItem}>
            <View>邮箱</View>
            <View>{userInfo.email}</View>
          </View>
          <View className={styles.listItem}>
            <View>微信</View>
            <View>
              {
                isLogin ?
                  <OpenData type="userNickName" /> : '-'
              }

            </View>
          </View>
        </View>
        <View className={styles.spaceInfo}>
          <View className={styles.spaceInfoBase}>
            <View className={styles.spaceName}>
              <View className={styles.grayTitle}>
                空间站
              </View>
              <View className={styles.spaceNames}>
                {spaceInfo.spaceName}
              </View>
            </View>
            <View className={styles.spaceImg}>
              <Image src={spaceInfo.spaceLogo} />
            </View>
          </View>
          <View className={styles.spaceAdmin}>
            <View className={styles.spaceItem}>
              <View className={styles.grayTitle}>
                创建时间
               </View>
              <View>
                {spaceInfo.createTime}
              </View>
            </View>
            <View className={styles.spaceItem}>
              <View className={styles.grayTitle}>
                创建人
              </View>
              <View >
                {spaceInfo.creator}
              </View>
            </View>
            <View className={styles.spaceItem}>
              <View className={styles.grayTitle}>
                管理员
              </View>
              <View >
                {spaceInfo.admin}
              </View>
            </View>
          </View>
          <View className={styles.grayTitle}>
            团队统计
          </View>
          <View className={styles.listItem}>
            <View>小组/部门</View>
            <View>{spaceInfo.departmentNumber}</View>
          </View>
          <View className={styles.listItem}>
            <View>总人数</View>
            <View>{spaceInfo.memberNumber}</View>
          </View>
          <View className={styles.grayTitle} style={{ marginTop: '14px' }}>
            空间站统计
          </View>
          <View className={styles.listItem}>
            <View>维格表</View>
            <View>{spaceInfo.sheetNumber}</View>
          </View>
          <View className={styles.listItem}>
            <View>总记录</View>
            <View>{spaceInfo.recordNumber}</View>
          </View>
          <View className={styles.listItem}>
            <View>附件空间</View>
            <View>{spaceInfo.attachmentSize}</View>
          </View>
        </View>
      </View>
      <View className={styles.remark}>
        <Text>
          更多功能请移步pc端体验
        </Text>
        <Text>
          vika.cn
        </Text>
      </View>
    </View>
  );
}

AuthPage.config = {
  navigationBarTitleText: '维格数表',
};
