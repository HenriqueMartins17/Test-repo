import { Dropdown, Menu, Space } from 'antd';
import { IUserProfile } from 'api/user';
import Link from 'next/link';
import { useState } from 'react';
import { colors } from '@apitable/components';
import { CheckOutlined, EmailOutlined, LogoutOutlined, Star2Outlined, StyleOutlined, WebOutlined } from '@apitable/icons';
import updateLanguage, { LanguageName } from '../../../../utils/language';
import { ThemeName } from '../../../../utils/theme';
import styles from './user-dropdown.module.scss';

interface IWorkspaceUserDropdownProps {
  user: IUserProfile;
  setTheme: (theme: ThemeName) => void;
}

export default function UserDropdown(props: IWorkspaceUserDropdownProps) {
  const [languageSelected, setLanguageSelected] = useState<LanguageName>(LanguageName.English);
  const [themeSelected, setThemeSelected] = useState<ThemeName>(ThemeName.Light);

  const updateCurrentLanguage = (language: LanguageName) => {
    setLanguageSelected(language);
    updateLanguage(language);
  };

  const updateCurrentTheme = (theme: ThemeName) => {
    setThemeSelected(theme);
    props.setTheme(theme);
  };

  const UserDropdownMenu = (
    <Menu className={styles.dropdown}>
      <Menu.Item key="userInfo">
        <div className={styles.dropdownUserInfo}>
          <img src={props.user.avatar || '/file/img/avatar.svg'} alt="user avatar" width={32} height={32} />
          <div>
            <div color={colors.textCommonPrimary}>{props.user.nickName}</div>
            <div style={{ color: colors.textCommonTertiary }}>{props.user.email}</div>
          </div>
        </div>
      </Menu.Item>
      <Menu.Item key="plan">
        <Link href="/workspace" passHref>
          <div className={styles.dropdownMenuItem}>
            <Star2Outlined color={colors.textCommonTertiary} />
            <div color={colors.textCommonPrimary}>Plan and billing</div>
          </div>
        </Link>
      </Menu.Item>
      {/* <Menu.Item key="email">
        <div className={styles.dropdownMenuItem}>
          <EmailOutlined color={colors.textCommonPrimary} />
          <div color={colors.textCommonPrimary}>Email</div>
        </div>
      </Menu.Item> */}
      <Menu.SubMenu
        title={
          <div className={styles.dropdownSubMenu}>
            <WebOutlined color={colors.textCommonTertiary} />
            <div
              style={{
                color: colors.textCommonPrimary,
              }}
            >
              Language
            </div>
          </div>
        }
        popupClassName={styles.dropdownSubMenuPopup}
      >
        <Menu.Item
          key={LanguageName.English}
          onClick={() => {
            updateCurrentLanguage(LanguageName.English);
          }}
        >
          <div className={styles.menuSubMenuItem}>
            <div color={colors.textCommonPrimary}>English</div>
            {languageSelected == LanguageName.English && <CheckOutlined color={colors.textBrandDefault} />}
          </div>
        </Menu.Item>
        <Menu.Item
          key={LanguageName.Chinese}
          onClick={() => {
            updateCurrentLanguage(LanguageName.Chinese);
          }}
          className={styles.menuSubMenuItem}
        >
          <div className={styles.menuSubMenuItem}>
            <div color={colors.textCommonPrimary}>中文</div>
            {languageSelected == LanguageName.Chinese && <CheckOutlined color={colors.textBrandDefault} />}
          </div>
        </Menu.Item>
      </Menu.SubMenu>
      <Menu.SubMenu
        title={
          <div className={styles.dropdownSubMenu}>
            <StyleOutlined color={colors.textCommonTertiary} />
            <div
              style={{
                color: colors.textCommonPrimary,
              }}
            >
              Dark Mode
            </div>
          </div>
        }
        popupClassName={styles.dropdownSubMenuPopup}
      >
        <Menu.Item
          key={ThemeName.Light}
          onClick={() => {
            updateCurrentTheme(ThemeName.Light);
          }}
        >
          <div className={styles.menuSubMenuItem}>
            <div color={colors.textCommonPrimary}>Light</div>
            {themeSelected == ThemeName.Light && <CheckOutlined color={colors.textBrandDefault} />}
          </div>
        </Menu.Item>
        <Menu.Item
          key={ThemeName.Dark}
          onClick={() => {
            updateCurrentTheme(ThemeName.Dark);
          }}
        >
          <div className={styles.menuSubMenuItem}>
            <div color={colors.textCommonPrimary}>Dark</div>
            {themeSelected == ThemeName.Dark && <CheckOutlined color={colors.textBrandDefault} />}
          </div>
        </Menu.Item>
      </Menu.SubMenu>
      <Menu.Item key="logOut">
        <Link href="/api/v1/airagent/logout" passHref>
          <div className={styles.dropdownMenuItem}>
            <LogoutOutlined color={colors.textCommonTertiary} />
            <div color={colors.textCommonPrimary}>Log out</div>
          </div>
        </Link>
      </Menu.Item>
    </Menu>
  );

  return (
    <Dropdown overlay={UserDropdownMenu} trigger={['click']} className={styles.userDropdownStyle}>
      <div style={{ cursor: 'pointer' }} onClick={(e) => e.preventDefault()}>
        <Space>
          <img src={props.user.avatar || '/file/img/avatar.svg'} alt="Logo" width={32} height={32} />
          <div>{props.user.nickName}</div>
        </Space>
      </div>
    </Dropdown>
  );
}
