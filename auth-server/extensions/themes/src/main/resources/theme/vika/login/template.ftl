<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false showAnotherWayIfPresent=true>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <meta name="description" content="维格统一登录页">
        <meta property="og:image" content="//s1.vika.cn/space/2021/09/16/f167b1d36c1f491ebfc288a888bbf1ff"/>
        <#if properties.meta?has_content>
            <#list properties.meta?split(' ') as meta>
                <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
            </#list>
        </#if>
        <title>${msg("loginTitle",(realm.displayName!''))}</title>
        <link rel="stylesheet" href="${url.resourcesPath}/node_modules/element-ui/lib/theme-chalk/index.css">
        <link rel="icon" href="${url.resourcesPath}/img/favicon.ico"/>
        <link rel="shortcut icon" href="//s4.vika.cn/web_build/favicon.ico"/>
        <#if properties.stylesCommon?has_content>
            <#list properties.stylesCommon?split(' ') as style>
                <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
                <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
            </#list>
        </#if>
        <#if scripts??>
            <#list scripts as script>
                <script src="${script}" type="text/javascript"></script>
            </#list>
        </#if>
    </head>

    <body class="${properties.kcBodyClass!}">
    <div class="${properties.kcLoginClass!}">
        <div id="kc-header" class="${properties.kcHeaderClass!}">
            <div id="kc-header-wrapper" class="${properties.kcHeaderWrapperClass!}">更简单，却更强大</div>
        </div>

        <div id="friendlyLink">
            <a href="https://vika.cn/chatgroup" target="_blank" id="kc-current-locale-link">意见反馈</a>
            <a href="https://vika.cn/help" target="_blank" id="kc-current-locale-link">帮助支持</a>
            <a href="https://vika.cn/?home=1" target="_blank" id="kc-current-locale-link">进入官网</a>
        </div>
        <div id="left-container">
            <div class="img"></div>
            <div class="codeGroup">
                <div class="codeItem">
                    <div class="qrCodeImage"></div>
                    <div class="qrCodeText">专属顾问</div>
                </div>
                <div class="codeItem">
                    <div class="qrCodeImage"></div>
                    <div class="qrCodeText">vika维格表</div>
                </div>
            </div>
        </div>
        <div id="right-container">
            <div class="${properties.kcFormCardClass!}">
                <header class="${properties.kcFormHeaderClass!}">
                    <h1 id="kc-page-title">${msg("loginAccountTitle")}</h1>
                </header>
                <div id="kc-content">
                    <div id="kc-content-wrapper">

                        <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                        <#-- during login.                                                                               -->
                        <#--<#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                            <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                                <div class="pf-c-alert__icon">
                                    <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                    <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                    <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                    <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                                </div>
                                    <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                            </div>
                        </#if>-->

                        <#nested "form">

                        <#if auth?has_content && auth.showTryAnotherWayLink() && showAnotherWayIfPresent>
                            <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                                <div class="${properties.kcFormGroupClass!}">
                                    <input type="hidden" name="tryAnotherWay" value="on"/>
                                    <a href="#" id="try-another-way"
                                       onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </body>
    </html>
</#macro>
