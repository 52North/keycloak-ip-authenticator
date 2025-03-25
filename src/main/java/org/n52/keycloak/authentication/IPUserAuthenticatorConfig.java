package org.n52.keycloak.authentication;

import org.jboss.logging.Logger;
import org.keycloak.models.AuthenticatorConfigModel;

import java.util.Map;

import static org.n52.keycloak.authentication.IPUserAuthenticatorFactory.*;

public class IPUserAuthenticatorConfig {
    private static final Logger LOG = Logger.getLogger(IPUserAuthenticatorConfig.class);

    private boolean useForwardedHeader;
    private String forwardedHeaderName;
    private int trustedProxiesCount;

    IPUserAuthenticatorConfig(AuthenticatorConfigModel configModel) {
        this(configModel.getConfig());
    }

    IPUserAuthenticatorConfig(Map<String, String> configMap) {
        this.useForwardedHeader = Boolean.parseBoolean(configMap.get(CONF_USE_FORWARDED_HEADER));
        this.forwardedHeaderName = configMap.getOrDefault(CONF_FORWARDED_HEADER_NAME, CONF_FORWARDED_HEADER_NAME_DEFAULT);
        this.trustedProxiesCount = Integer.parseInt(configMap.get(CONF_TRUSTED_PROXIES_COUNT));
    }

    public boolean isUseForwardedHeader() {
        return useForwardedHeader;
    }

    public void setUseForwardedHeader(boolean useForwardedHeader) {
        this.useForwardedHeader = useForwardedHeader;
    }

    public String getForwardedHeaderName() {
        return forwardedHeaderName;
    }

    public void setForwardedHeaderName(String forwardedHeaderName) {
        this.forwardedHeaderName = forwardedHeaderName;
    }

    public int getTrustedProxiesCount() {
        return trustedProxiesCount;
    }

    public void setTrustedProxiesCount(int trustedProxiesCount) {
        this.trustedProxiesCount = trustedProxiesCount;
    }
}
