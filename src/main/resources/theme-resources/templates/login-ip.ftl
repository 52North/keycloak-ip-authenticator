<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<div id="ip-auth">
    <@layout.registrationLayout displayMessage=!messagesPerField.existsError('password'); section>
    <!-- template: login-ip.ftl -->
        <#if section = "header">
            ${msg("loginAccountTitle")}
        <#elseif section = "form">
            <div class="${properties.kcContentWrapperClass}">
                ${msg("ipAuthText")}
            </div>
            <div id="kc-form">
                <div id="kc-form-wrapper">
                    <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                        <@buttons.loginButton />
                    </form>
                </div>
            </div>
        </#if>
    </@layout.registrationLayout>
</div>