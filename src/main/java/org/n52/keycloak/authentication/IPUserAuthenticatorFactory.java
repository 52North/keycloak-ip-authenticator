package org.n52.keycloak.authentication;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.keycloak.provider.ProviderConfigProperty.*;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class IPUserAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "ip-user-authenticator";

    static final String CONF_USE_FORWARDED_HEADER = "use-forwarded-header";
    static final String CONF_FORWARDED_HEADER_NAME = "forwarded-header-name";
    static final String CONF_FORWARDED_HEADER_NAME_DEFAULT = "X-Forwarded-For";
    static final String CONF_TRUSTED_PROXIES_COUNT = "trusted-proxies-count";

    public static final IPUserAuthenticator SINGLETON = new IPUserAuthenticator();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "IP Authenticator";
    }

    @Override
    public String getHelpText() {
        return "Validates a user by his current IP address.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        final ProviderConfigProperty useForwardedHeader = new ProviderConfigProperty();
        useForwardedHeader.setType(BOOLEAN_TYPE);
        useForwardedHeader.setName(CONF_USE_FORWARDED_HEADER);
        useForwardedHeader.setLabel("Use a 'forwarded' header");
        useForwardedHeader.setHelpText(format("Use IP addresses from a header added by reverse proxies (e.g. {0}). Be aware of security concerns when using this header and set 'Number of trusted proxies' accordingly (https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For#security_and_privacy_concerns)",
                CONF_FORWARDED_HEADER_NAME_DEFAULT));

        final ProviderConfigProperty forwardedHeaderName = new ProviderConfigProperty();
        forwardedHeaderName.setType(STRING_TYPE);
        forwardedHeaderName.setName(CONF_FORWARDED_HEADER_NAME);
        forwardedHeaderName.setLabel("Forwarded header name");
        forwardedHeaderName.setDefaultValue(CONF_FORWARDED_HEADER_NAME_DEFAULT);
        forwardedHeaderName.setRequired(false);
        forwardedHeaderName.setHelpText(format("Optional: Name of the forwarded header (Default: {0})", CONF_FORWARDED_HEADER_NAME_DEFAULT));

        final ProviderConfigProperty trustedProxiesCount = new ProviderConfigProperty();
        trustedProxiesCount.setType(STRING_TYPE);
        trustedProxiesCount.setName(CONF_TRUSTED_PROXIES_COUNT);
        trustedProxiesCount.setLabel("Number of trusted proxies");
        trustedProxiesCount.setDefaultValue("1");
        trustedProxiesCount.setHelpText("Number of trusted proxies in your network. Only the last n ip addresses from the forwarded header will be used.");

        return Arrays.asList(useForwardedHeader, forwardedHeaderName, trustedProxiesCount);
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}
