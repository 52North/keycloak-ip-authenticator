# Keycloak IP Authenticator
This Keycloak extension enables user authentication by a client's IP address. You can use the extension as a 
standalone authentication step within your authentication flow so that users do not have to use username and 
password for authentication.

## Authentication Flow
The IP Authenticator works as follows:
1. The client IP is fetched from HTTP request header or from an HTTP forwarded header if configured. If no client IP
could be fetched, authentication fails.
2. All users that are enabled for IP authentication via custom user profile attributes are discovered.
3. Supported IP ranges, which are also set as custom user profile attributes, are validated against the client IP to 
find matching ones. For multiple hits, only the first user is selected.
4. The selected user is validated for proper configuration and set as authenticated.

## Credits
This project is inspired by the great https://github.com/evosec/keycloak-ipaddress-authenticator project. If you look
for conditional authentication flows based on the client's IP, you are right there.