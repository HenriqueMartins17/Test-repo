<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "form">
        <script src="${url.resourcesPath}/node_modules/vue/dist/vue.js"></script>
        <script src="${url.resourcesPath}/node_modules/element-ui/lib/index.js"></script>
        <script src="${url.resourcesPath}/node_modules/axios/dist/axios.js"></script>
        <script src="https://g.alicdn.com/AWSC/AWSC/awsc.js"></script>
        <script src="https://g.alicdn.com/dingding/dinglogin/0.0.5/ddLogin.js"></script>
        <div id="vue-app">
            <div class="loader" id="form_loading" style="display: flex"></div>
            <div class="login-content" style="display:none">
                <div v-if="isQuickLogin" class="quick-login-plus">
                    <div style="display:flex;justify-content: center"><img src="${url.resourcesPath}/img/datasheet_img_welcome.png" style="height: 180px; margin-bottom: 34.8px"/></div>
                    <div v-if="isWechat" style="display: flex;flex-direction: column;padding: 0px 0px 0;padding-left: 24px;padding-right: 24px;">
                        <button class="wechat-login-button" v-on:click="wechatLogin('${social.providers[0].loginUrl}')">
                            <svg width="16px" height="16px" viewBox="0 0 24 24" class="sc-bdvvaa sc-gsDJrp  jeaabY" fill="#C4C4C4" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M2.49908 17.9998C3.70773 17.8306 4.9346 16.9935 5.24435 16.6088C6.71113 17.0397 8.05643 17.1843 9.26811 17.1074C9.00813 16.5079 9.01466 15.7628 9.02047 15.0998C9.02131 15.0038 9.02213 14.9096 9.02213 14.8177C9.02213 10.9647 12.7331 7.67796 16.9178 7.91493C16.9846 7.918 17.0454 7.92724 17.1 7.9457C16.3378 4.55739 12.8607 2 8.68201 2C3.94156 2 0.100006 5.28983 0.100006 9.34596C0.100006 12.8174 1.82187 14.51 3.60448 15.4486C3.37193 16.4236 3.06235 16.9754 2.7747 17.4881C2.67921 17.6583 2.58612 17.8242 2.49908 17.9998ZM3.85001 7.03905C3.85001 6.34769 4.40864 5.78906 5.10001 5.78906C5.78861 5.78906 6.35001 6.34769 6.35001 7.03905C6.35001 7.72765 5.79138 8.28904 5.10001 8.28904C4.40864 8.28904 3.85001 7.73042 3.85001 7.03905ZM10.35 8.50046C11.0414 8.50046 11.6 7.93908 11.6 7.25048C11.6 6.55911 11.0414 6.00049 10.35 6.00049C9.65864 6.00049 9.10001 6.55911 9.10001 7.25048C9.10001 7.94184 9.65864 8.50046 10.35 8.50046ZM21.5647 21.9999C21.2846 21.463 20.8335 20.3008 20.8983 19.9999C22.2635 19.3745 23.8971 17.7728 23.9 14.9586C23.9 11.6666 20.7656 9 16.9 9C13.0344 9 9.89999 11.7817 9.89999 15.292C9.89999 18.7934 13.4029 22.2536 19.5655 20.9999C20.2703 21.6075 21.0693 21.8878 21.5647 21.9999ZM17.4 13.3359C17.4 12.7814 17.8454 12.3359 18.4 12.3359C18.9546 12.3359 19.4 12.7814 19.4 13.3359C19.4 13.8905 18.9546 14.3359 18.4 14.3359C17.8454 14.3359 17.4 13.8905 17.4 13.3359ZM14.4 12.3242C13.8454 12.3242 13.4 12.7696 13.4 13.3242C13.4 13.8788 13.8454 14.3242 14.4 14.3242C14.9546 14.3242 15.4 13.8788 15.4 13.3242C15.4 12.7696 14.9516 12.3242 14.4 12.3242Z" fill="#FFFFFF" fill-rule="evenodd" clip-rule="evenodd"></path></svg>
                            <span style="margin-left:4px">微信登录</span>
                        </button>
                        <button style="margin-top: 16px" v-on:click="changeQuickLogin">
                            <svg viewBox="0 0 24 24" width="24px" fill="#8C8C8C" height="24px"><path d="M17 5h-6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h6c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm1 12c0 .6-.4 1-1 1h-6c-.6 0-1-.4-1-1V7c0-.6.4-1 1-1h1c0 .6.4 1 1 1h2c.6 0 1-.4 1-1h1c.6 0 1 .4 1 1v10z"></path><path d="M15.5 16h-3c-.3 0-.5.2-.5.5s.2.5.5.5h3c.3 0 .5-.2.5-.5s-.2-.5-.5-.5z"></path></svg>
                            <span style="margin-left:4px">更多登录方式</span>
                        </button>
                    </div>
                    <div v-if="isDingTalk" style="display: flex;flex-direction: column;padding: 0px 0px 0;padding-left: 24px;padding-right: 24px;">
                        <button class="dingTalk-login-button" v-on:click="dingTalkLogin('${social.providers[1].loginUrl}')">
                            <svg width="16px" height="16px" viewBox="0 0 24 24" class="sc-hKwCoD sc-eCImvq fgBDdK cXbbhc" fill="#FFFFFF" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M20.9596 9.10305C20.9195 9.26986 20.8213 9.51366 20.6874 9.81306L20.674 9.843C19.8753 11.5667 17.7914 14.9414 17.7914 14.9414L17.7825 14.92L17.1712 15.9893H20.1073L14.4938 23.5L15.7656 18.3888H13.4541L14.2573 15.0055C13.6103 15.1638 12.8383 15.3819 11.928 15.6728C11.928 15.6728 10.6964 16.3999 8.385 14.2741C8.385 14.2741 6.82321 12.8883 7.72459 12.5419C8.10834 12.3922 9.59427 12.204 10.7634 12.05C12.343 11.8362 13.3113 11.7207 13.3113 11.7207C13.3113 11.7207 8.44747 11.7977 7.29175 11.6137C6.13602 11.4298 4.6724 9.49655 4.36451 7.78568C4.36451 7.78568 3.88258 6.84898 5.40421 7.29381C6.92138 7.73436 13.2132 9.02178 13.2132 9.02178C13.2132 9.02178 5.02492 6.49398 4.48053 5.87807C3.93167 5.26643 2.87411 2.51621 3.01244 0.82673C3.01244 0.82673 3.07045 0.407567 3.49883 0.518774C3.49883 0.518774 9.54519 3.30321 13.6862 4.82161C17.8271 6.35283 21.4237 7.127 20.9596 9.10305Z" fill="inherit"></path></svg>
                            <span style="margin-left:4px">钉钉登录</span>
                        </button>
                        <button style="margin-top: 16px" v-on:click="changeQuickLogin">
                            <svg viewBox="0 0 24 24" width="24px" fill="#8C8C8C" height="24px"><path d="M17 5h-6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h6c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm1 12c0 .6-.4 1-1 1h-6c-.6 0-1-.4-1-1V7c0-.6.4-1 1-1h1c0 .6.4 1 1 1h2c.6 0 1-.4 1-1h1c.6 0 1 .4 1 1v10z"></path><path d="M15.5 16h-3c-.3 0-.5.2-.5.5s.2.5.5.5h3c.3 0 .5-.2.5-.5s-.2-.5-.5-.5z"></path></svg>
                            <span style="margin-left:4px">更多登录方式</span>
                        </button>
                    </div>
                    <div v-if="isFeishu" style="display: flex;flex-direction: column;padding: 0px 0px 0;padding-left: 24px;padding-right: 24px;">
                        <button class="feishu-login-button" v-on:click="feishuLogin('${social.providers[2].loginUrl}')">
                            <svg width="16px" height="16px" viewBox="0 0 24 24" class="sc-hKwCoD sc-eCImvq fgBDdK cXbbhc" fill="#FFFFFF" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M1 7.87488C1 7.24089 1.42195 6.68442 2.03231 6.51347L21.7183 1L21.6887 1.02957L21.7247 1.00439L16.0051 6.71704C15.9396 6.70421 15.872 6.69748 15.8028 6.69748C15.2257 6.69748 14.7578 7.16535 14.7578 7.74249C14.7578 7.81127 14.7644 7.87849 14.7771 7.94355L10.1139 12.601L5.13281 12.596L5.13379 12.5953L1.41398 8.87456C1.14891 8.60943 1 8.24983 1 7.87488ZM15.6274 22.5146C16.2551 22.5146 16.806 22.0925 16.9752 21.482L22.4515 1.76807L16.8112 7.46767C16.835 7.55518 16.8477 7.64725 16.8477 7.74228C16.8477 8.31943 16.3797 8.7873 15.8026 8.7873C15.7114 8.7873 15.6229 8.77561 15.5386 8.75366L10.9443 13.3962V18.3699L10.9453 18.3708V18.4072L10.9601 18.3858L14.6377 22.1005C14.9002 22.3656 15.2562 22.5146 15.6274 22.5146Z" fill="inherit" fill-rule="evenodd" clip-rule="evenodd"></path></svg>
                            <span style="margin-left:4px">飞书登录</span>
                        </button>
                        <button style="margin-top: 16px" v-on:click="changeQuickLogin">
                            <svg viewBox="0 0 24 24" width="24px" fill="#8C8C8C" height="24px"><path d="M17 5h-6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h6c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm1 12c0 .6-.4 1-1 1h-6c-.6 0-1-.4-1-1V7c0-.6.4-1 1-1h1c0 .6.4 1 1 1h2c.6 0 1-.4 1-1h1c.6 0 1 .4 1 1v10z"></path><path d="M15.5 16h-3c-.3 0-.5.2-.5.5s.2.5.5.5h3c.3 0 .5-.2.5-.5s-.2-.5-.5-.5z"></path></svg>
                            <span style="margin-left:4px">更多登录方式</span>
                        </button>
                    </div>
                    <div v-if="isQQ" style="display: flex;flex-direction: column;padding: 0px 0px 0;padding-left: 24px;padding-right: 24px;">
                        <button class="qq-login-button" v-on:click="qqLogin('${social.providers[3].loginUrl}')">
                            <svg width="16px" height="16px" viewBox="0 0 24 24" class="sc-hKwCoD sc-eCImvq fgBDdK cXbbhc" fill="#FFFFFF" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M2.68506 13.6885C1.90502 15.6388 1.7701 17.4791 2.38885 17.8091C2.81893 18.0555 3.51885 17.5066 4.16396 16.5177C4.4064 17.6166 5.05151 18.578 5.93907 19.3744C4.99775 19.7319 4.379 20.3358 4.379 20.9947C4.379 22.0936 6.04659 23 8.08943 23C9.94571 23 11.4794 22.285 11.7746 21.3247H12.2057C12.5008 22.2861 14.0346 23 15.8898 23C17.9611 23 19.6002 22.12 19.6002 20.9947C19.6002 20.3347 18.9825 19.7319 18.0412 19.3744C18.9288 18.578 19.5739 17.6166 19.8163 16.5177C20.4614 17.5077 21.1339 18.0555 21.5914 17.8091C22.2365 17.4791 22.1027 15.6113 21.2963 13.6885C20.6765 12.1782 19.8437 11.0518 19.1986 10.8054C19.1986 10.7229 19.225 10.6129 19.225 10.5029C19.225 9.9265 19.0637 9.3765 18.7938 8.9376V8.8276C18.7938 8.5526 18.7411 8.3062 18.6326 8.0862C18.4713 4.1306 15.9973 1 11.9896 1C7.98297 1 5.50899 4.1306 5.34666 8.0862C5.23682 8.3163 5.18151 8.57056 5.18538 8.8276V8.9376C4.91659 9.3776 4.75531 9.9265 4.75531 10.5029V10.8054C4.16396 11.0518 3.30276 12.1507 2.68506 13.6885Z" fill="inherit"></path></svg>
                            <span style="margin-left:4px">QQ登录</span>
                        </button>
                        <button style="margin-top: 16px" v-on:click="changeQuickLogin">
                            <svg viewBox="0 0 24 24" width="24px" fill="#8C8C8C" height="24px"><path d="M17 5h-6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h6c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm1 12c0 .6-.4 1-1 1h-6c-.6 0-1-.4-1-1V7c0-.6.4-1 1-1h1c0 .6.4 1 1 1h2c.6 0 1-.4 1-1h1c.6 0 1 .4 1 1v10z"></path><path d="M15.5 16h-3c-.3 0-.5.2-.5.5s.2.5.5.5h3c.3 0 .5-.2.5-.5s-.2-.5-.5-.5z"></path></svg>
                            <span style="margin-left:4px">更多登录方式</span>
                        </button>
                    </div>
                    <div id="recordLinkMobile">
                        <a href="https://beian.miit.gov.cn" target="_blank" id="kc-current-locale-link">粤ICP备 19106018号</a>
                        <a href="http://www.beian.gov.cn/portal/registerSystemInfo" target="_blank" id="kc-current-locale-link">公安备案 44030402004286</a>
                    </div>
                </div>
                <div v-if="!isQuickLogin">
                    <div id="kc-form">
                        <#if realm.password && social.providers??>
                            <div class="title-label"><span class="title-label-content">${msg("identity-provider-login-label")}</span></div>
                            <div class="quick-login-wapper quick-login-pc-wrapper ${properties.kcFormSocialAccountListClass!}">
                                <#list social.providers as p>
                                    <#if p.providerId == "wechat_open">
                                        <img v-on:click="wechatLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_wechat.png" class="quick-login-img"/>
                                    </#if>
                                    <#if p.providerId == "ding_talk">
                                        <img v-on:click="dingTalkLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_dingding.png" class="quick-login-img"/>
                                    </#if>
                                    <#if p.providerId == "feishu">
                                        <img v-on:click="feishuLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_feishu.png" class="quick-login-img"/>
                                    </#if>
                                    <#if p.providerId == "qq">
                                        <img v-on:click="qqLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_qq.png" class="quick-login-img"/>
                                    </#if>
                                </#list>
                            </div>
                        </#if>
                        <div class="title-label">
                            <span class="title-label-content" v-if="passwordLogin">密码登录</span>
                            <span class="title-label-content" v-if="!passwordLogin">验证码登录</span>
                        </div>
                        <div id="kc-form-wrapper">
                            <#if realm.password>
                                <form id="kc-form-login" onsubmit="login.disabled = true; return true;"
                                      action="${url.loginAction}" method="post">
                                    <div class="${properties.kcLabelWrapperClass!} tabContainer">
                                        <div v-bind:class="{ active: phoneLogin, tabItem: true }"
                                             v-on="!phoneLogin ? { click: changeAccountType } : {}">
                                            ${msg("phone")}
                                        </div>
                                        <div v-bind:class="{ active: !phoneLogin, tabItem: true }"
                                             v-on="phoneLogin ? { click: changeAccountType } : {}">
                                            ${msg("email")}
                                        </div>
                                    </div>
                                    <#--密码登录-->
                                    <div v-if="passwordLogin">
                                        <#--手机密码登录-->
                                        <div v-if="phoneLogin">
                                            <div class="${properties.kcFormGroupClass!}">
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'pwd_phone'}"
                                                     v-bind:ariaInvalid="accountPasswordError === '' ? false : true">
                                                    <el-dropdown @command="areaCodeChange" trigger="click"
                                                                 placement="bottom-start"
                                                                 @visible-change="areaCodeVisibleChange">
                                                        <span class="el-dropdown-link">
                                                             {{areaCode}}
                                                            <i v-bind:class="{areaCodeArrow:true, visible: areaDrowdownVisible}"></i>
                                                        </span>
                                                        <el-dropdown-menu slot="dropdown">
                                                            <el-dropdown-item v-for="(item,index) in areaOptions"
                                                                              v-text="item.label"
                                                                              :command="item"
                                                                              :key="index"
                                                                              v-bind:class="{active: item.value == areaCode}"
                                                            ></el-dropdown-item>
                                                        </el-dropdown-menu>
                                                    </el-dropdown>
                                                    <div class="input phoneInput">
                                                        <#if usernameEditDisabled??>
                                                            <input v-model.trim="phoneNumber" tabindex="1" id="phoneNumber"
                                                                   class="${properties.kcInputClass!}" v-model="phoneNumber"
                                                                   name="phoneNumber" type="text" disabled
                                                            />
                                                        <#else>
                                                            <input v-model.trim="phoneNumber"
                                                                   @focus="curFousedInput = 'pwd_phone'"
                                                                   @blur="curFousedInput = ''"
                                                                   @input="initErrorTips"
                                                                   tabindex="1" id="phoneNumber"
                                                                   class="${properties.kcInputClass!}" name="phoneNumber"
                                                                   v-model="phoneNumber" type="text" autofocus
                                                                   placeholder="请输入手机号"
                                                                   autocomplete="off"
                                                                   oninput="value=value.replace(/[^\d]/g,'')"/>
                                                        </#if>
                                                    </div>
                                                    <#if messagesPerField.existsError('accountPassword')>
                                                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{accountPasswordError}}
                                                        </span>
                                                    </#if>
                                                </div>
                                            </div>

                                            <div class="${properties.kcFormGroupClass!}">
                                                <span for="password"
                                                      class="${properties.kcLabelClass!}">${msg("password")}</span>
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'pwd_pwd'}"
                                                     v-bind:ariaInvalid="accountPasswordError === '' ? false : true">
                                                    <div class="input passwordInput">
                                                        <input
                                                                v-bind:type="passwordType"
                                                                v-model="password"
                                                                tabindex="2"
                                                                @focus="curFousedInput = 'pwd_pwd'"
                                                                @blur="curFousedInput = ''"
                                                                @input="initErrorTips"
                                                                placeholder="请输入密码"
                                                                id="password" class="${properties.kcInputClass!}"
                                                                @input="initErrorTips" name="password" autocomplete="off"/>
                                                    </div>
                                                    <i v-bind:class="{passwordVisibleIcon:true, visible: passwordType=='text'}"
                                                       v-on:click="passwordTypeChange"></i>
                                                    <#if messagesPerField.existsError('accountPassword')>
                                                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{accountPasswordError}}
                                                        </span>
                                                    </#if>
                                                </div>
                                            </div>
                                        </div>
                                        <#--邮箱密码登录-->
                                        <div v-if="!phoneLogin">
                                            <div class="${properties.kcFormGroupClass!}">
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'pwd_email'}"
                                                     v-bind:ariaInvalid="Boolean(accountPasswordError||emailInputError)">
                                                    <div class="input emailInput">
                                                        <#if usernameEditDisabled??>
                                                            <input v-model.trim="email" tabindex="1" id="email"
                                                                   class="${properties.kcInputClass!}" v-model="email"
                                                                   name="email" type="text" disabled/>
                                                        <#else>
                                                            <input v-model.trim="email"
                                                                   @focus="curFousedInput = 'pwd_email'"
                                                                   @blur="curFousedInput = ''"
                                                                   tabindex="1" id="email" @input="initErrorTips"
                                                                   class="${properties.kcInputClass!}" name="email" type="text"
                                                                   placeholder="请输入邮箱"
                                                                   v-model="email" autocomplete="off"/>
                                                        </#if>
                                                    </div>
                                                    <#if messagesPerField.existsError('email')>
                                                        <span id="email-input-error"
                                                              class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{emailInputError}}
                                                        </span>
                                                    </#if>
                                                    <#if messagesPerField.existsError('accountPassword')>
                                                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{accountPasswordError}}
                                                        </span>
                                                    </#if>
                                                </div>
                                            </div>

                                            <div class="${properties.kcFormGroupClass!}">
                                                <span for="password"
                                                      class="${properties.kcLabelClass!}">${msg("password")}</span>
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'pwd_pwd'}"
                                                     v-bind:ariaInvalid="accountPasswordError === '' ? false : true">
                                                    <div class="input passwordInput">
                                                        <input v-bind:type="passwordType"
                                                               @focus="curFousedInput = 'pwd_pwd'"
                                                               @blur="curFousedInput = ''"
                                                               v-model="password" tabindex="2" @input="initErrorTips"
                                                               id="password" class="${properties.kcInputClass!}" name="password"
                                                               placeholder="请输入密码"
                                                               v-model="password" autocomplete="off"/>
                                                    </div>
                                                    <i v-bind:class="{passwordVisibleIcon:true, visible: passwordType=='text'}"
                                                       v-on:click="passwordTypeChange"></i>
                                                    <#if messagesPerField.existsError('accountPassword')>
                                                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{accountPasswordError}}
                                                        </span>
                                                    </#if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <#--验证码登录-->
                                    <div v-if="!passwordLogin">
                                        <#--手机验证码登录-->
                                        <div v-if="phoneLogin">
                                            <div class="${properties.kcFormGroupClass!}">
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'code_phone'}"
                                                     v-bind:ariaInvalid="(accountPasswordError === '' && phoneInputError === '') ? false : true">
                                                    <el-dropdown @command="areaCodeChange" trigger="click"
                                                                 placement="bottom-start"
                                                                 @visible-change="areaCodeVisibleChange">
                                                <span class="el-dropdown-link">
                                                     {{areaCode}}
                                                    <i v-bind:class="{areaCodeArrow:true, visible: areaDrowdownVisible}"></i>
                                                </span>
                                                        <el-dropdown-menu slot="dropdown">
                                                            <el-dropdown-item v-for="(item,index) in areaOptions"
                                                                              v-text="item.label"
                                                                              :command="item"
                                                                              v-bind:class="{active: item.value == areaCode}"
                                                            ></el-dropdown-item>
                                                        </el-dropdown-menu>
                                                    </el-dropdown>
                                                    <div class="input phoneInput">
                                                        <#if usernameEditDisabled??>
                                                            <input v-model.trim="phoneNumber" tabindex="1" id="phoneNumber"
                                                                   class="${properties.kcInputClass!}" name="phoneNumber"
                                                                   v-model="phoneNumber" type="text" disabled/>
                                                        <#else>
                                                            <input v-model.trim="phoneNumber"
                                                                   @focus="curFousedInput = 'code_phone'"
                                                                   @blur="curFousedInput = ''"
                                                                   tabindex="1" id="phoneNumber" @input="initErrorTips"
                                                                   class="${properties.kcInputClass!}" name="phoneNumber"
                                                                   type="text" v-model="phoneNumber"
                                                                   placeholder="请输入手机号"
                                                                   oninput="value=value.replace(/[^\d]/g,'')"/>
                                                        </#if>
                                                    </div>
                                                    <#if messagesPerField.existsError('accountPassword')>
                                                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                                              aria-live="polite">
                                                            {{accountPasswordError}}
                                                        </span>
                                                    </#if>
                                                    <span id="phone-input-error" class="${properties.kcInputErrorMessageClass!}"
                                                          aria-live="polite">
                                                        {{phoneInputError}}
                                                    </span>
                                                </div>
                                            </div>

                                            <div class="${properties.kcFormGroupClass!}">
                                                <label for="password" class="${properties.kcLabelClass!}">验证码</label>
                                                <div class="verificationCodeItem">
                                                    <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'code_code'}"
                                                         v-bind:ariaInvalid="codeInputError === '' ? false : true">
                                                        <div class="input codeInput">
                                                            <input tabindex="2"
                                                                   @focus="curFousedInput = 'code_code'"
                                                                   @blur="curFousedInput = ''"
                                                                   maxlength="6"
                                                                   placeholder="请输入验证码"
                                                                   v-model="code" @input="initErrorTips" id="code"
                                                                   class="${properties.kcInputClass!}" name="code"
                                                                   v-model="code" autocomplete="off"/>
                                                        </div>
                                                    </div>
                                                    <button
                                                            class="verificationCodeButton"
                                                            type="button"
                                                            v-on:click="sendVerificationCode"
                                                            :disabled='smsCodeBtnLoading || !(phoneNumber || email) || sendButtonText !== initSendButtonText'
                                                    >
                                                        <div class="loader" v-if="smsCodeBtnLoading"></div>
                                                        {{smsCodeBtnLoading ? '' : sendButtonText}}
                                                    </button>
                                                    <span id="code-input-error" class="${properties.kcInputErrorMessageClass!}"
                                                          aria-live="polite">
                                                        {{codeInputError}}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <#--邮箱验证码登录-->
                                        <div v-if="!phoneLogin">
                                            <div class="${properties.kcFormGroupClass!}">
                                                <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'code_email'}"
                                                     v-bind:ariaInvalid="emailInputError === '' ? false : true">
                                                    <div class="input emailInput">
                                                        <#if usernameEditDisabled??>
                                                            <input v-model.trim="email" tabindex="1" id="email"
                                                                   class="${properties.kcInputClass!}" name="email"
                                                                   v-model="email" type="text" disabled/>
                                                        <#else>
                                                            <input v-model.trim="email"
                                                                   @focus="curFousedInput = 'code_email'"
                                                                   @blur="curFousedInput = ''"
                                                                   tabindex="1" id="email" @input="initErrorTips"
                                                                   class="${properties.kcInputClass!}" name="email" type="text"
                                                                   placeholder="请输入邮箱"
                                                                   v-model="email" autofocus/>
                                                        </#if>
                                                    </div>
                                                    <span id="email-input-error" class="${properties.kcInputErrorMessageClass!}"
                                                          aria-live="polite">
                                                        {{emailInputError}}
                                                    </span>
                                                </div>
                                            </div>

                                            <div class="${properties.kcFormGroupClass!}">
                                                <label for="password" class="${properties.kcLabelClass!}">验证码</label>
                                                <div class="verificationCodeItem">
                                                    <div v-bind:class="{inputWrapper: true, focused: curFousedInput === 'code_code'}"
                                                         v-bind:ariaInvalid="codeInputError === '' ? false : true">
                                                        <div class="input codeInput">
                                                            <input tabindex="2"
                                                                   @focus="curFousedInput = 'code_code'"
                                                                   @blur="curFousedInput = ''"
                                                                   maxlength="6"
                                                                   placeholder="请输入验证码"
                                                                   v-model="code" id="code" @input="initErrorTips"
                                                                   class="${properties.kcInputClass!}" name="code"
                                                                   v-model="code" autocomplete="off"/>
                                                        </div>
                                                    </div>
                                                    <button
                                                            class="verificationCodeButton"
                                                            type="button"
                                                            v-on:click="sendVerificationCode"
                                                            :disabled='emailCodeBtnLoading || !(phoneNumber || email) || sendButtonText !== initSendButtonText'
                                                    >
                                                        <div class="loader" v-if="emailCodeBtnLoading"></div>
                                                        {{emailCodeBtnLoading ? '' : sendButtonText}}
                                                    </button>
                                                    <span id="code-input-error" class="${properties.kcInputErrorMessageClass!}"
                                                          aria-live="polite">
                                                        {{codeInputError}}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                                        <input type="hidden" id="id-hidden-input" name="credentialId"
                                               <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                                        <input tabindex="4"
                                               class="submitButton ${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                               name="login"
                                               id="kc-login"
                                               type="submit"
                                               value="${msg("doLogIn")}"
                                               :disabled="submitBtnDisabled"
                                        />
                                    </div>
                                    <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                                        <div id="kc-form-options">
                                            <div v-if="passwordLogin" v-on:click="changeLoginType" class="vika-primary-btn">验证码登录</div>
                                            <div v-if="!passwordLogin" v-on:click="changeLoginType" class="vika-primary-btn">密码登录</div>
                                            <#if realm.rememberMe && !usernameEditDisabled??>
                                                <div class="checkbox">
                                                    <label>
                                                        <#if login.rememberMe??>
                                                            <input tabindex="3" id="rememberMe" name="rememberMe"
                                                                   type="checkbox" checked> ${msg("rememberMe")}
                                                        <#else>
                                                            <input tabindex="3" id="rememberMe" name="rememberMe"
                                                                   type="checkbox"> ${msg("rememberMe")}
                                                        </#if>
                                                    </label>
                                                </div>
                                            </#if>
                                            <div class="${properties.kcFormOptionsWrapperClass!}">
                                                <#if realm.resetPasswordAllowed>
                                                    <span><a tabindex="5" href="${loginResetCredentialsUrl}" target="_blank"
                                                             class="resetBtn vika-primary-btn">${msg("doForgotPassword")}</a></span>
                                                </#if>
                                            </div>
                                        </div>
                                    </div>
                                    <div id="recordLinkPC">
                                        <a href="https://beian.miit.gov.cn" target="_blank" id="kc-current-locale-link">粤ICP备 19106018号</a>
                                        <a href="http://www.beian.gov.cn/portal/registerSystemInfo" target="_blank" id="kc-current-locale-link">公安备案 44030402004286</a>
                                    </div>
                                </form>
                            </#if>
                        </div>
                        <div class="quick-login-wapper quick-login-mobile-wrapper ${properties.kcFormSocialAccountListClass!}">
                            <#list social.providers as p>
                                <#if p.providerId == "wechat_open">
                                    <img v-on:click="wechatLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_wechat.png" class="quick-login-img"/>
                                </#if>
                                <#if p.providerId == "ding_talk">
                                    <img v-on:click="dingTalkLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_dingding.png" class="quick-login-img"/>
                                </#if>
                                <#if p.providerId == "feishu">
                                    <img v-on:click="feishuLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_feishu.png" class="quick-login-img"/>
                                </#if>
                                <#if p.providerId == "qq">
                                    <img v-on:click="qqLogin('${p.loginUrl}')" src="${url.resourcesPath}/img/signin_img_qq.png" class="quick-login-img"/>
                                </#if>
                            </#list>
                        </div>
                        <el-dialog
                                title="温馨提示"
                                :visible.sync="verifyRejectTipVisible"
                                width="416px"
                                class="verifyRejectModal"
                        >
                            <span>当前环境存在风险，请重新验证</span>
                            <span slot="footer" class="dialog-footer">
                            <el-button type="primary" @click="closeVerifyRejectTip">知道了</el-button>
                        </span>
                        </el-dialog>
                        <el-dialog
                            title="安全验证"
                            :visible.sync="verifyModalVisible"
                            width="388px"
                            class="verifyModal"
                            @opened="verifyModalOpened"
                        >
                            <span>请拖动下方滑块完成验证</span>
                            <span slot="footer" class="dialog-footer">
                            <div id="nc"></div>
                        </span>
                        </el-dialog>
                        <el-dialog
                            title="快速安全登录"
                            :visible.sync="wechatLoginVisible"
                            width="320px"
                            class="socialLoginModal"
                            @opened="socialLoginOpened"
                            @closed="socialLoginClosed"
                            center
                        >
                            <div class="codeWrapper">
                                <div class="freshMask" id="freshMaskDiv" style="display: none" @click="refreshQrcode(qrCodeMark)">
                                    <span>二维码已失效</span>
                                    <span>请点击刷新</span>
                                </div>
                                <img id="wechatMpQrCode"/>
                            </div>
                            <span class="wechat-login-msg">请使用微信扫码并关注公众号</span>
                        </el-dialog>
                        <el-dialog
                                title="快速安全登录"
                                :visible.sync="dingTalkLoginVisible"
                                width="320px"
                                class="dingTalkLoginModal"
                                @opened="dingTalkLoginOpened"
                                @closed="socialLoginClosed"
                                center
                        >
                            <div id="dingTalkQrcode" class="dingTalkLogin"></div>
                        </el-dialog>
                    </div>
                </div>
            </div>
        </div>
        <script type="module">
            var app = new Vue({
                el: '#vue-app',
                data: {
                    areaCode: '+86',
                    freezeSendCodeSeconds: 0,
                    areaDrowdownVisible: false,
                    phoneLogin: sessionStorage.getItem('phoneLogin') === 'false' ? false : true,
                    passwordVisible: false,
                    passwordType: 'password',
                    passwordLogin: sessionStorage.getItem('passwordLogin') === 'false' ? false : true,
                    sendButtonText: '获取验证码',
                    initSendButtonText: '获取验证码',
                    phoneNumber: "${login.phoneNumber!''}",
                    email: "${login.email!''}",
                    password: "${login.password!''}",
                    code: "${login.code!''}",
                    accountPasswordError: "${kcSanitize(messagesPerField.getFirstError('accountPassword'))?no_esc}",
                    phoneInputError: '',
                    emailInputError: "${kcSanitize(messagesPerField.getFirstError('email'))?no_esc}",
                    codeInputError: "${kcSanitize(messagesPerField.getFirstError('code'))?no_esc}".replace('&#xff0c;', '，'),
                    curFousedInput: '',
                    wechatMpQrCodeUrl: '',
                    qrCodeMark: '',
                    dingTalkQrCodeUrl: '',
                    state: '',
                    verifyRejectTipVisible: false,
                    verifyModalVisible: false,
                    wechatLoginVisible: false,
                    dingTalkLoginVisible: false,
                    smsCodeBtnLoading: false,
                    emailCodeBtnLoading: false,
                    accountPasswordHasError: "${kcSanitize(messagesPerField.getFirstError('accountPassword'))?no_esc}" === '' ? "false" : "true",
                    areaOptions: [
                        {"value": "+86", "label": "中国大陆（+86）"},
                        {"value": "+852", "label": "中国香港（+852）"},
                        {"value": "+853", "label": "中国澳门（+853）"},
                        {"value": "+886", "label": "中国台湾（+886）"},
                        {"value": "+355", "label": "阿尔巴尼亚（+355）"},
                        {"value": "+213", "label": "阿尔及利亚（+213）"},
                        {"value": "+93", "label": "阿富汗（+93）"},
                        {"value": "+54", "label": "阿根廷（+54）"},
                        {"value": "+971", "label": "阿拉伯联合酋长国（+971）"},
                        {"value": "+297", "label": "阿鲁巴（+297）"},
                        {"value": "+968", "label": "阿曼（+968）"},
                        {"value": "+994", "label": "阿塞拜疆（+994）"},
                        {"value": "+20", "label": "埃及（+20）"},
                        {"value": "+251", "label": "埃塞俄比亚（+251）"},
                        {"value": "+353", "label": "爱尔兰（+353）"},
                        {"value": "+372", "label": "爱沙尼亚（+372）"},
                        {"value": "+376", "label": "安道尔（+376）"},
                        {"value": "+244", "label": "安哥拉（+244）"},
                        {"value": "+1264", "label": "安圭拉（+1264）"},
                        {"value": "+1268", "label": "安提瓜和巴布达（+1268）"},
                        {"value": "+43", "label": "奥地利（+43）"},
                        {"value": "+61", "label": "澳大利亚（+61）"},
                        {"value": "+1246", "label": "巴巴多斯（+1246）"},
                        {"value": "+675", "label": "巴布亚新几内亚（+675）"},
                        {"value": "+1242", "label": "巴哈马（+1242）"},
                        {"value": "+92", "label": "巴基斯坦（+92）"},
                        {"value": "+595", "label": "巴拉圭（+595）"},
                        {"value": "+970", "label": "巴勒斯坦（+970）"},
                        {"value": "+973", "label": "巴林（+973）"},
                        {"value": "+507", "label": "巴拿马（+507）"},
                        {"value": "+55", "label": "巴西（+55）"},
                        {"value": "+375", "label": "白俄罗斯（+375）"},
                        {"value": "+1441", "label": "百慕大群岛（+1441）"},
                        {"value": "+359", "label": "保加利亚（+359）"},
                        {"value": "+229", "label": "贝宁（+229）"},
                        {"value": "+32", "label": "比利时（+32）"},
                        {"value": "+354", "label": "冰岛（+354）"},
                        {"value": "+1787", "label": "波多黎各（+1787）"},
                        {"value": "+48", "label": "波兰（+48）"},
                        {"value": "+387", "label": "波斯尼亚和黑塞哥维那（+387）"},
                        {"value": "+591", "label": "玻利维亚（+591）"},
                        {"value": "+501", "label": "伯利兹（+501）"},
                        {"value": "+267", "label": "博茨瓦纳（+267）"},
                        {"value": "+975", "label": "不丹（+975）"},
                        {"value": "+226", "label": "布基纳法索（+226）"},
                        {"value": "+257", "label": "布隆迪（+257）"},
                        {"value": "+240", "label": "赤道几内亚（+240）"},
                        {"value": "+45", "label": "丹麦（+45）"},
                        {"value": "+49", "label": "德国（+49）"},
                        {"value": "+670", "label": "东帝汶（+670）"},
                        {"value": "+228", "label": "多哥（+228）"},
                        {"value": "+1767", "label": "多米尼加（+1767）"},
                        {"value": "+1809", "label": "多米尼加共和国（+1809）"},
                        {"value": "+7", "label": "俄罗斯（+7）"},
                        {"value": "+593", "label": "厄瓜多尔（+593）"},
                        {"value": "+291", "label": "厄立特里亚（+291）"},
                        {"value": "+33", "label": "法国（+33）"},
                        {"value": "+298", "label": "法罗群岛（+298）"},
                        {"value": "+689", "label": "法属波利尼西亚（+689）"},
                        {"value": "+594", "label": "法属圭亚那（+594）"},
                        {"value": "+63", "label": "菲律宾（+63）"},
                        {"value": "+679", "label": "斐济（+679）"},
                        {"value": "+358", "label": "芬兰（+358）"},
                        {"value": "+220", "label": "冈比亚（+220）"},
                        {"value": "+242", "label": "刚果共和国（+242）"},
                        {"value": "+243", "label": "刚果民主共和国（+243）"},
                        {"value": "+57", "label": "哥伦比亚（+57）"},
                        {"value": "+506", "label": "哥斯达黎加（+506）"},
                        {"value": "+1473", "label": "格林纳达（+1473）"},
                        {"value": "+299", "label": "格陵兰岛（+299）"},
                        {"value": "+995", "label": "格鲁吉亚（+995）"},
                        {"value": "+53", "label": "古巴（+53）"},
                        {"value": "+590", "label": "瓜德罗普岛（+590）"},
                        {"value": "+502", "label": "瓜地马拉（+502）"},
                        {"value": "+1671", "label": "关岛（+1671）"},
                        {"value": "+592", "label": "圭亚那（+592）"},
                        {"value": "+7", "label": "哈萨克斯坦（+7）"},
                        {"value": "+509", "label": "海地（+509）"},
                        {"value": "+82", "label": "韩国（+82）"},
                        {"value": "+31", "label": "荷兰（+31）"},
                        {"value": "+382", "label": "黑山（+382）"},
                        {"value": "+504", "label": "洪都拉斯（+504）"},
                        {"value": "+686", "label": "基里巴斯（+686）"},
                        {"value": "+253", "label": "吉布提（+253）"},
                        {"value": "+996", "label": "吉尔吉斯斯坦（+996）"},
                        {"value": "+224", "label": "几内亚（+224）"},
                        {"value": "+245", "label": "几内亚比绍共和国（+245）"},
                        {"value": "+1", "label": "加拿大（+1）"},
                        {"value": "+233", "label": "加纳（+233）"},
                        {"value": "+241", "label": "加蓬（+241）"},
                        {"value": "+855", "label": "柬埔寨（+855）"},
                        {"value": "+420", "label": "捷克（+420）"},
                        {"value": "+263", "label": "津巴布韦（+263）"},
                        {"value": "+237", "label": "喀麦隆（+237）"},
                        {"value": "+974", "label": "卡塔尔（+974）"},
                        {"value": "+1345", "label": "开曼群岛（+1345）"},
                        {"value": "+238", "label": "开普（+238）"},
                        {"value": "+269", "label": "科摩罗（+269）"},
                        {"value": "+965", "label": "科威特（+965）"},
                        {"value": "+385", "label": "克罗地亚（+385）"},
                        {"value": "+254", "label": "肯尼亚（+254）"},
                        {"value": "+682", "label": "库克群岛（+682）"},
                        {"value": "+599", "label": "库拉索（+599）"},
                        {"value": "+371", "label": "拉脱维亚（+371）"},
                        {"value": "+266", "label": "莱索托（+266）"},
                        {"value": "+856", "label": "老挝（+856）"},
                        {"value": "+961", "label": "黎巴嫩（+961）"},
                        {"value": "+370", "label": "立陶宛（+370）"},
                        {"value": "+231", "label": "利比里亚（+231）"},
                        {"value": "+218", "label": "利比亚（+218）"},
                        {"value": "+423", "label": "列支敦士登（+423）"},
                        {"value": "+262", "label": "留尼汪（+262）"},
                        {"value": "+352", "label": "卢森堡（+352）"},
                        {"value": "+250", "label": "卢旺达（+250）"},
                        {"value": "+40", "label": "罗马尼亚（+40）"},
                        {"value": "+261", "label": "马达加斯加（+261）"},
                        {"value": "+960", "label": "马尔代夫（+960）"},
                        {"value": "+356", "label": "马耳他（+356）"},
                        {"value": "+265", "label": "马拉维（+265）"},
                        {"value": "+60", "label": "马来西亚（+60）"},
                        {"value": "+223", "label": "马里（+223）"},
                        {"value": "+389", "label": "马其顿（+389）"},
                        {"value": "+596", "label": "马提尼克（+596）"},
                        {"value": "+269", "label": "马约特（+269）"},
                        {"value": "+230", "label": "毛里求斯（+230）"},
                        {"value": "+222", "label": "毛里塔尼亚（+222）"},
                        {"value": "+1", "label": "美国（+1）"},
                        {"value": "+1684", "label": "美属萨摩亚（+1684）"},
                        {"value": "+1284", "label": "美属维尔京群岛（+1284）"},
                        {"value": "+976", "label": "蒙古（+976）"},
                        {"value": "+1664", "label": "蒙特塞拉特岛（+1664）"},
                        {"value": "+880", "label": "孟加拉国（+880）"},
                        {"value": "+51", "label": "秘鲁（+51）"},
                        {"value": "+95", "label": "缅甸（+95）"},
                        {"value": "+373", "label": "摩尔多瓦（+373）"},
                        {"value": "+212", "label": "摩洛哥（+212）"},
                        {"value": "+377", "label": "摩纳哥（+377）"},
                        {"value": "+258", "label": "莫桑比克（+258）"},
                        {"value": "+52", "label": "墨西哥（+52）"},
                        {"value": "+264", "label": "纳米比亚（+264）"},
                        {"value": "+27", "label": "南非（+27）"},
                        {"value": "+505", "label": "尼加拉瓜（+505）"},
                        {"value": "+977", "label": "尼泊尔（+977）"},
                        {"value": "+227", "label": "尼日尔（+227）"},
                        {"value": "+234", "label": "尼日利亚（+234）"},
                        {"value": "+47", "label": "挪威（+47）"},
                        {"value": "+680", "label": "帕劳（+680）"},
                        {"value": "+351", "label": "葡萄牙（+351）"},
                        {"value": "+81", "label": "日本（+81）"},
                        {"value": "+46", "label": "瑞典（+46）"},
                        {"value": "+41", "label": "瑞士（+41）"},
                        {"value": "+503", "label": "萨尔瓦多（+503）"},
                        {"value": "+685", "label": "萨摩亚（+685）"},
                        {"value": "+381", "label": "塞尔维亚（+381）"},
                        {"value": "+232", "label": "塞拉利昂（+232）"},
                        {"value": "+221", "label": "塞内加尔（+221）"},
                        {"value": "+357", "label": "塞浦路斯（+357）"},
                        {"value": "+248", "label": "塞舌尔（+248）"},
                        {"value": "+966", "label": "沙特阿拉伯（+966）"},
                        {"value": "+508", "label": "圣彼埃尔和密克隆岛（+508）"},
                        {"value": "+239", "label": "圣多美和普林西比（+239）"},
                        {"value": "+1869", "label": "圣基茨和尼维斯（+1869）"},
                        {"value": "+1758", "label": "圣露西亚（+1758）"},
                        {"value": "+1721", "label": "圣马丁岛（荷兰部分）（+1721）"},
                        {"value": "+378", "label": "圣马力诺（+378）"},
                        {"value": "+1784", "label": "圣文森特和格林纳丁斯（+1784）"},
                        {"value": "+94", "label": "斯里兰卡（+94）"},
                        {"value": "+421", "label": "斯洛伐克（+421）"},
                        {"value": "+386", "label": "斯洛文尼亚（+386）"},
                        {"value": "+268", "label": "斯威士兰（+268）"},
                        {"value": "+249", "label": "苏丹（+249）"},
                        {"value": "+597", "label": "苏里南（+597）"},
                        {"value": "+677", "label": "所罗门群岛（+677）"},
                        {"value": "+252", "label": "索马里（+252）"},
                        {"value": "+992", "label": "塔吉克斯坦（+992）"},
                        {"value": "+66", "label": "泰国（+66）"},
                        {"value": "+255", "label": "坦桑尼亚（+255）"},
                        {"value": "+676", "label": "汤加（+676）"},
                        {"value": "+1649", "label": "特克斯和凯科斯群岛（+1649）"},
                        {"value": "+1868", "label": "特立尼达和多巴哥（+1868）"},
                        {"value": "+216", "label": "突尼斯（+216）"},
                        {"value": "+90", "label": "土耳其（+90）"},
                        {"value": "+993", "label": "土库曼斯坦（+993）"},
                        {"value": "+678", "label": "瓦努阿图（+678）"},
                        {"value": "+58", "label": "委内瑞拉（+58）"},
                        {"value": "+673", "label": "文莱（+673）"},
                        {"value": "+256", "label": "乌干达（+256）"},
                        {"value": "+380", "label": "乌克兰（+380）"},
                        {"value": "+598", "label": "乌拉圭（+598）"},
                        {"value": "+998", "label": "乌兹别克斯坦（+998）"},
                        {"value": "+34", "label": "西班牙（+34）"},
                        {"value": "+30", "label": "希腊（+30）"},
                        {"value": "+225", "label": "象牙海岸（+225）"},
                        {"value": "+65", "label": "新加坡（+65）"},
                        {"value": "+687", "label": "新喀里多尼亚（+687）"},
                        {"value": "+64", "label": "新西兰（+64）"},
                        {"value": "+36", "label": "匈牙利（+36）"},
                        {"value": "+963", "label": "叙利亚（+963）"},
                        {"value": "+1876", "label": "牙买加（+1876）"},
                        {"value": "+374", "label": "亚美尼亚（+374）"},
                        {"value": "+967", "label": "也门（+967）"},
                        {"value": "+964", "label": "伊拉克（+964）"},
                        {"value": "+98", "label": "伊朗（+98）"},
                        {"value": "+972", "label": "以色列（+972）"},
                        {"value": "+39", "label": "意大利（+39）"},
                        {"value": "+91", "label": "印度（+91）"},
                        {"value": "+62", "label": "印度尼西亚（+62）"},
                        {"value": "+44", "label": "英国（+44）"},
                        {"value": "+1340", "label": "英属处女群岛（+1340）"},
                        {"value": "+962", "label": "约旦（+962）"},
                        {"value": "+84", "label": "越南（+84）"},
                        {"value": "+260", "label": "赞比亚（+260）"},
                        {"value": "+235", "label": "乍得（+235）"},
                        {"value": "+350", "label": "直布罗陀（+350）"},
                        {"value": "+56", "label": "智利（+56）"},
                        {"value": "+236", "label": "中非共和国（+236）"}
                    ],
                    isWechat: navigator.userAgent.toLowerCase().indexOf('micromessenger') !== -1,
                    isDingTalk: navigator.userAgent.indexOf('DingTalk') > -1,
                    isQQ: navigator.userAgent.toLowerCase().indexOf(' qq') > -1,
                    isFeishu: navigator.userAgent.toLowerCase().indexOf('feishu') > -1,
                },
                computed: {
                    submitBtnDisabled: function () {
                        if (this.passwordLogin && this.phoneLogin) {
                            return Boolean(!this.phoneNumber || !this.password || this.phoneInputError || this.accountPasswordError)
                        } else if ((!this.passwordLogin) && this.phoneLogin) {
                            return Boolean(!this.phoneNumber || !this.code || this.phoneInputError || this.codeInputError)
                        } else if (this.passwordLogin && !this.phoneLogin) {
                            return Boolean(!this.email || !this.password || this.emailInputError || this.accountPasswordError)
                        } else if (!this.passwordLogin && !this.phoneLogin) {
                            return Boolean(!this.email || !this.code || this.emailInputError || this.codeInputError)
                        }
                        return false
                    },
                    isQuickLogin: function(){
                        return this.isWechat || this.isDingTalk || this.isQQ || this.isFeishu
                    }
                },
                methods: {
                    feishuLogin: function (url) {
                        window.location.href = url;
                    },
                    qqLogin: function (url) {
                        window.location.href = url;
                    },
                    dingTalkLogin: function (url) {
                        if (/DingTalk/i.test(window.navigator.userAgent)) {
                            window.location.href = url;
                        } else {
                            axios({
                                method: 'GET',
                                url: window.location.origin + url,
                            }).then(res => {
                                const {status, data} = res;
                                this.state = this.getUrlKey(data, 'state');
                                if (status === 200) {
                                    this.dingTalkLoginVisible = true;
                                    this.dingTalkQrCodeUrl = data;
                                }
                            })
                        }
                    },
                    handleMessage: function (event) {
                        const origin = event.origin;
                        console.log("origin", event.origin);
                        //判断是否来自ddLogin扫码事件。
                        if(origin == "https://login.dingtalk.com") {
                            const loginTmpCode = event.data;
                            //获取到loginTmpCode后就可以在这里构造跳转链接进行跳转了
                            console.log("loginTmpCode", loginTmpCode);
                            const redirectUrl = window.location.origin + '/auth/realms/${realm.name}/broker/ding_talk/endpoint';
                            window.location.href = 'https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoabk6kuj5klzw5nsbo&response_type=code&scope=snsapi_login&state='+this.state+'&redirect_uri=' + redirectUrl + '&loginTmpCode=' + loginTmpCode;
                        }
                    },
                    wechatLogin: function (url) {
                        if (/MicroMessenger/i.test(window.navigator.userAgent)) {
                            window.location.href = url;
                        } else {
                            axios({
                                method: 'GET',
                                url: window.location.origin + url,
                            }).then(res => {
                                const {status, data} = res;
                                let mark = this.getUrlKey(data, 'mark');
                                let redirectUri = this.getUrlKey(data, 'redirect_uri');
                                let state = this.getUrlKey(data, 'state');
                                if (status === 200) {
                                    this.qrCodeMark = mark;
                                    this.wechatLoginVisible = true;
                                    this.wechatMpQrCodeUrl = data;
                                    setTimeout(function () {
                                        document.getElementById("freshMaskDiv").style.display = "";
                                    }, 600000);
                                    this.timer = window.setInterval(function () {
                                        axios({
                                            method: 'POST',
                                            url: window.location.origin + '/auth/realms/${realm.name}/broker/wechat_open/endpoint/poll?mark=' + mark,
                                        }).then(res => {
                                            const {code, data} = res.data;
                                            if (code === 200) {
                                                window.location.href = redirectUri + "?openid=" + data + "&state=" + state;
                                            }
                                        })
                                    }, 3000);
                                }
                            })
                        }
                    },
                    refreshQrcode(qrCodeMark) {
                        axios({
                            method: 'GET',
                            url: window.location.origin + '/auth/realms/${realm.name}/broker/wechat_open/endpoint/qrcode?mark=' + qrCodeMark,
                        }).then(res => {
                            const {status, data} = res;
                            document.getElementById("freshMaskDiv").style.display = "none";
                            if (status === 200) {
                                this.wechatMpQrCodeUrl = data;
                                const wechatMpQrCode = document.querySelector('#wechatMpQrCode');
                                wechatMpQrCode.setAttribute("src", this.wechatMpQrCodeUrl);
                                setTimeout(function () {
                                    document.getElementById("freshMaskDiv").style.display = "";
                                }, 600000);
                            }
                        })
                    },
                    getUrlKey: function (url, name) {
                        return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(url) || [, ""])[1].replace(/\+/g, '%20')) || null
                    },
                    socialLoginClosed () {
                        location.reload();
                    },
                    areaCodeChange(command) {
                        this.areaCode = command.value
                    },
                    areaCodeVisibleChange(visible) {
                        this.areaDrowdownVisible = visible
                    },
                    passwordTypeChange() {
                        this.passwordType = this.passwordType == 'text' ? 'password' : 'text';
                    },
                    initNoTraceVerification(successCallback) {
                        AWSC.use("nvc", function (state, module) {
                            window.nvc = module.init({
                                appkey: "FFFF0N00000000008B7D",
                                scene: "nvc_login",
                                success: function (data) {
                                    successCallback(data)
                                },
                                isH5: false,
                                popUp: false,
                                renderTo: '#captcha',
                                language: 'cn',
                                customWidth: '100%',
                                upLang: {
                                    cn: {
                                        _startTEXT: '请按住滑块，拖动到最右边',
                                        _yesTEXT: '验证通过',
                                        _error300: '哎呀，出错了，点击<a href=\"javascript:__nc.reset()\">刷新</a>再来一次',
                                        _errorNetwork: '网络不给力，请<a href=\"javascript:__nc.reset()\">点击刷新</a>',
                                    },
                                },
                                // test: module.TEST_PASS, // 无痕验证通过
                                // test: module.TEST_BLOCK, // 无痕验证未通过，直接拦截 250
                                // test: module.TEST_NC_PASS, // 唤醒滑动验证，且滑动验证通过 251
                                // test: module.TEST_NC_BLOCK, // 唤醒滑动验证，且滑动验证不通过 251
                            });
                        });
                    },
                    initFormValue() {
                        this.email = '';
                        this.phoneNumber = '';
                        this.code = '';
                        this.password = '';
                    },
                    initErrorTips() {
                        this.accountPasswordError = '';
                        this.codeInputError = '';
                        this.emailInputError = '';
                        this.phoneInputError = '';
                    },
                    changeLoginType() {
                        app.passwordLogin = !app.passwordLogin
                        sessionStorage.setItem("passwordLogin", app.passwordLogin);
                        this.initFormValue();
                        this.initErrorTips();
                    },
                    changeAccountType() {
                        app.phoneLogin = !app.phoneLogin
                        sessionStorage.setItem("phoneLogin", app.phoneLogin);
                        this.initFormValue();
                        this.initErrorTips();
                    },
                    closeVerifyRejectTip() {
                        this.verifyRejectTipVisible = false
                        window['nvc'].reset();
                    },
                    verifyModalOpened() {
                        const nc = document.querySelector('#nc')
                        while (nc.hasChildNodes()) {
                            nc.removeChild(nc.children[0]);
                        }
                        window['nvc'].getNC({renderTo: 'nc'});
                    },
                    socialLoginOpened() {
                        const wechatMpQrCode = document.querySelector('#wechatMpQrCode');
                        wechatMpQrCode.setAttribute("src", this.wechatMpQrCodeUrl);
                    },
                    dingTalkLoginOpened() {
                        const obj = DDLogin({
                            id: "dingTalkQrcode",
                            goto: encodeURIComponent(this.dingTalkQrCodeUrl),
                            style: "border:none;background-color:#FFFFFF;",
                            width : "210",
                            height: "210"
                        });
                    },
                    disableSend: function (seconds) {
                        if (seconds <= 0) {
                            app.sendButtonText = app.initSendButtonText;
                            app.freezeSendCodeSeconds = 0;
                        } else {
                            app.sendButtonText = String(seconds) + ' 秒';
                            setTimeout(function () {
                                app.disableSend(seconds - 1);
                            }, 1000);
                        }
                    },
                    getSmsCode() {
                        const phoneNumber = document.getElementById('phoneNumber')
                        if (!phoneNumber) return;
                        app.smsCodeBtnLoading = true,
                            window.nvc.getNVCValAsync(function (nvcVal) {
                                axios({
                                    method: 'POST',
                                    url: window.location.origin + '/auth/realms/${realm.name}/sms/code',
                                    data: {
                                        phoneNumber: phoneNumber.value.trim(),
                                        recaptchaData: nvcVal,
                                        areaCode: app.areaCode,
                                    },
                                    headers: {
                                        'Content-Type': 'application/x-www-form-urlencoded'
                                    },
                                    transformRequest: [function (data) {
                                        let ret = ''
                                        for (let it in data) {
                                            ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                                        }
                                        return ret
                                    }],
                                }).then(res => {
                                    app.smsCodeBtnLoading = false;
                                    const {code, message} = res.data;
                                    // 进行二次验证（滑块验证）
                                    if (code === 251 || code === 250) {
                                        app.verifyModalVisible = true;
                                        return;
                                        // 开始验证
                                    } else if (code === 252) {
                                        app.verifyRejectTipVisible = true
                                        return;
                                    } else if (code === 230 || code === 233) {
                                        app.codeInputError = message;
                                        return;
                                    } else if (code === 303) {
                                        app.phoneInputError = message;
                                        return;
                                    } else if (code === 200) {
                                        app.phoneInputError = '';
                                        app.codeInputError = '';
                                    }
                                    app.disableSend(60)
                                }).catch(err => {
                                    console.log(err)
                                })
                            })
                    },
                    getEmailCode() {
                        const email = document.getElementById('email')
                        if (!email) return;
                        app.emailCodeBtnLoading = true;
                        axios({
                            method: 'POST',
                            url: window.location.origin + '/auth/realms/${realm.name}/mail/code',
                            data: {
                                email: email.value.trim(),
                            },
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            transformRequest: [function (data) {
                                let ret = ''
                                for (let it in data) {
                                    ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                                }
                                return ret
                            }],
                        }).then(res => {
                            app.emailCodeBtnLoading = false;
                            const {code, message} = res.data;
                            if (code === 230 || code === 233) {
                                this.codeInputError = message;
                            } else if (code === 500) {
                                this.emailInputError = message;
                                return;
                            } else if (code === 200) {
                                this.emailInputError = '';
                                this.codeInputError = '';
                            }
                            app.disableSend(60)
                        })
                    },
                    sendVerificationCode: function (e) {
                        // 获取手机号验证码
                        if (!this.passwordLogin && this.phoneLogin) {
                            app.getSmsCode();
                            return;
                        }
                        if (!this.passwordLogin && !this.phoneLogin) {
                            // 获取邮箱验证码
                            app.getEmailCode();
                            return;
                        }
                    },
                    changeQuickLogin() {
                        const recordLinkMobile = document.getElementById('recordLinkMobile');
                        recordLinkMobile.style.display = 'none';
                        app.isWechat = false;
                        app.isFeishu = false;
                        app.isQQ = false;
                        app.isDingTalk = false;
                    }
                }
            });
            const formDom = document.querySelector('.login-content');
            const boxDom = document.querySelector('#form_loading');
            formDom.style.display = 'block';
            boxDom.style.display = 'none';

            app.initNoTraceVerification(app.getSmsCode)

            if (typeof window.addEventListener != 'undefined') {
                window.addEventListener('message', app.handleMessage, false);
            } else if (typeof window.attachEvent != 'undefined') {
                window.attachEvent('onmessage', app.handleMessage);
            }

            console.log(app.isQuickLogin)
        </script>
    </#if>

</@layout.registrationLayout>
