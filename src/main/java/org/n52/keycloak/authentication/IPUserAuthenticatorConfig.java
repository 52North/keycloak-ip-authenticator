package org.n52.keycloak.authentication;

import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.models.AuthenticatorConfigModel;
import static org.n52.keycloak.authentication.IPUserAuthenticatorFactory.CONF_FORWARDED_HEADER_NAME;
import static org.n52.keycloak.authentication.IPUserAuthenticatorFactory.CONF_FORWARDED_HEADER_NAME_DEFAULT;
import static org.n52.keycloak.authentication.IPUserAuthenticatorFactory.CONF_TRUSTED_PROXIES_COUNT;
import static org.n52.keycloak.authentication.IPUserAuthenticatorFactory.CONF_USE_FORWARDED_HEADER;

public class IPUserAuthenticatorConfig {

    private static final Logger LOG = Logger.getLogger(IPUserAuthenticatorConfig.class);
    private static final int DEFAULT_TRUSTED_PROXIES_COUNT = 1;

    private boolean useForwardedHeader;
    private String forwardedHeaderName;
    private int trustedProxiesCount;

    IPUserAuthenticatorConfig(AuthenticatorConfigModel configModel) {
        this(configModel.getConfig());
    }

    IPUserAuthenticatorConfig(Map<String, String> configMap) {
        this.useForwardedHeader = Boolean.parseBoolean(configMap.get(CONF_USE_FORWARDED_HEADER));
        this.forwardedHeaderName = configMap.getOrDefault(CONF_FORWARDED_HEADER_NAME, CONF_FORWARDED_HEADER_NAME_DEFAULT);
        this.trustedProxiesCount = parseTrustedProxiesCount(configMap);
    }

    private int parseTrustedProxiesCount(Map<String, String> configMap) {
        String trustedProxiesCountStr = configMap.getOrDefault(CONF_TRUSTED_PROXIES_COUNT, String.valueOf(DEFAULT_TRUSTED_PROXIES_COUNT));
        
        if (trustedProxiesCountStr == null || trustedProxiesCountStr.trim().isEmpty()) {
            LOG.warn("Configuration value for 'trusted-proxies-count' is missing or empty. Using default value: " + DEFAULT_TRUSTED_PROXIES_COUNT);
            return DEFAULT_TRUSTED_PROXIES_COUNT;
        }
        
        try {
            int count = Integer.parseInt(trustedProxiesCountStr.trim());
            
            if (count < 1) {
                LOG.warn("Configuration value for 'trusted-proxies-count' must be >= 1. Got: " + count + ". Using default value: " + DEFAULT_TRUSTED_PROXIES_COUNT);
                return DEFAULT_TRUSTED_PROXIES_COUNT;
            }
            
            return count;
        } catch (NumberFormatException e) {
            LOG.warn("Configuration value for 'trusted-proxies-count' is not a valid integer: '" + trustedProxiesCountStr + "'. Using default value: " + DEFAULT_TRUSTED_PROXIES_COUNT, e);
            return DEFAULT_TRUSTED_PROXIES_COUNT;
        }
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
