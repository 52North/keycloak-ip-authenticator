
package org.n52.keycloak.authentication;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.format.IPAddressRange;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

/**
 * Helper class to deal with IP addresses
 */
public class IPAddressHelper {

    private static final Logger LOG = Logger.getLogger(IPAddressHelper.class);

    public static Optional<IPAddress> getClientIpAddress(AuthenticationFlowContext context, IPUserAuthenticatorConfig config) {

        if (config.isUseForwardedHeader()) {
            return getClientIpAddressFromForwardedHeader(context, config);
        } else {
            final String ipAddressStringFromConnection = context.getConnection().getRemoteAddr();
            return parseIpAddress(ipAddressStringFromConnection);
        }
    }

    public static Optional<IPAddress> getClientIpAddressFromForwardedHeader(AuthenticationFlowContext context, IPUserAuthenticatorConfig config) {

        final List<String> forwardedHeaders = context.getHttpRequest()
                .getHttpHeaders()
                .getRequestHeader(config.getForwardedHeaderName().trim());
        LOG.debug(format("Forwarded headers: {0}", forwardedHeaders));
        if (forwardedHeaders == null) {
            return Optional.empty();
        }

        final List<String> ipAddressesFromHeader = forwardedHeaders.stream()
                .map(h -> h.split(","))
                .flatMap(Arrays::stream)
                .toList();

        if (ipAddressesFromHeader.isEmpty()) {
            return Optional.empty();
        }

        final int trustedProxiesCount = config.getTrustedProxiesCount();
        final int numberOfIpAddressesInHeader = ipAddressesFromHeader.size();
        if (numberOfIpAddressesInHeader < trustedProxiesCount) {
            LOG.warn(format("Forwarded header contains less addresses than number of trusted proxies. Not possible to securely determine client ip address. Headers: {0}",
                    forwardedHeaders));
            return Optional.empty();
        }

        final int index = numberOfIpAddressesInHeader - trustedProxiesCount;
        final String firstTrustedIpAddress = ipAddressesFromHeader.get(index);
        return parseIpAddress(firstTrustedIpAddress);
    }

    public static Optional<IPAddress> parseIpAddress(String text) {

        IPAddressString ipAddressString = new IPAddressString(text.trim());

        if (!ipAddressString.isValid()) {
            LOG.warn("Ignoring invalid IP address " + ipAddressString);
            return Optional.empty();
        }

        try {
            final IPAddress parsedIpAddress = ipAddressString.toAddress();
            return Optional.of(parsedIpAddress);
        } catch (AddressStringException e) {
            LOG.warn("Ignoring invalid IP address " + ipAddressString, e);
            return Optional.empty();
        }
    }

    public static Stream<IPAddressRange> getIpAddressRanges(List<String> ipRanges){
        return ipRanges.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(IPAddressHelper::parseIpAddress)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
