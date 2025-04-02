package org.n52.keycloak.authentication;


import inet.ipaddr.IPAddress;
import inet.ipaddr.format.IPAddressRange;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IPUserAuthenticator extends AbstractUsernameFormAuthenticator {

    private static final Logger LOG = Logger.getLogger(IPUserAuthenticator.class);

    private static final String IP_AUTH_ENABLED_ATTR = "ipAuthEnabled";
    private static final String IP_AUTH_RANGES_ATTR = "ipAuthRanges";
    private static final String INVALID_IP_ERROR_MESSAGE = "invalid-ip-error";
    private static final String LOGIN_FORM = "login-ip.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        //TODO: Check if we need remember me form data
        Response challenge = context.form()
                .createForm(LOGIN_FORM);
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        IPUserAuthenticatorConfig config = new IPUserAuthenticatorConfig(context.getAuthenticatorConfig());
        Optional<IPAddress> clientIpAddress = IPAddressHelper.getClientIpAddress(context, config);

        if (clientIpAddress.isPresent()) {
            boolean validated = validateIpAndIdentifyUser(context, clientIpAddress.get());
            if (!validated) {
                return;
            }
            context.success();
        } else {
            LOG.warn(MessageFormat.format("Missing IP address in HTTP forwarded header ''{0}''.", config.getForwardedHeaderName()));
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge(context, INVALID_IP_ERROR_MESSAGE));
        }
    }

    private boolean validateIpAndIdentifyUser(AuthenticationFlowContext context, IPAddress ipAddress) {
        context.clearUser();
        UserModel user = identifyUser(context, ipAddress);
        if(user != null) {
            LOG.debug(MessageFormat.format("Identified user ''{0}'' for IP ''{1}''", user.getId(), ipAddress.toAddressString().toString()));
        }
        return user != null  && validateUser(context, user);
    }

    private UserModel identifyUser(AuthenticationFlowContext context, IPAddress ipAddress) {
        UserModel user = findUserByIp(context, ipAddress);
        if(user == null) {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge(context, INVALID_IP_ERROR_MESSAGE));
        }
        return user;
    }

    private UserModel findUserByIp(AuthenticationFlowContext context, IPAddress ipAddress) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        Map<String, String> params = Collections.singletonMap(IP_AUTH_ENABLED_ATTR, "true");

        Stream<UserModel> userStream = session.users().searchForUserStream(realm, params);
        //TODO check if multiple users exists for IP and setDuplicateUserChallenge if applicable
        List<UserModel> userCandidates = userStream.filter(u -> matchesIp(u, ipAddress)).toList();
        if(userCandidates.isEmpty()){
            LOG.warn(MessageFormat.format("No matching user found for IP ''{0}''.", ipAddress.toAddressString().toString()));
            return null;
        } else {
            UserModel user = userCandidates.get(0);
            if(userCandidates.size() > 1){
                LOG.warn(MessageFormat.format("Recognized multiple users for IP ''{0}''. Only the first user with ID ''{1}'' will be selected.", ipAddress.toAddressString().toString(), user.getId()));
                LOG.debug(MessageFormat.format("Recognized users: {0}",
                        userCandidates.stream().map(UserModel::getId).collect(Collectors.joining())));
            }
            return user;
        }
    }

    private boolean matchesIp(UserModel user, IPAddress ipAddress) {
        List<String> ipRanges = user.getAttributes().get(IP_AUTH_RANGES_ATTR);
        if (ipRanges == null || ipRanges.isEmpty()) {
            return false;
        }
        Stream<IPAddressRange> ipAddressRanges = IPAddressHelper.getIpAddressRanges(ipRanges);
        return ipAddressRanges.anyMatch(ipRange -> ipRange.contains(ipAddress));
    }

    private boolean validateUser(AuthenticationFlowContext context, UserModel user) {
        if (!enabledUser(context, user)) {
            return false;
        }
        //TODO Check if we need remember me handling
        LOG.debug(MessageFormat.format("Validated user ''{0}''.", user.getId()));
        context.setUser(user);
        return true;
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error != null) {
            form.setError(error);
        }
        return context.form()
                .createForm(LOGIN_FORM);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
