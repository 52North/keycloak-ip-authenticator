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

## Deployment
You can build the project from source by running `mvn clean package`and provide the resulting JAR artifact in
the Keycloak plugins directory of your Keycloak instance. For more, read the [Keycloak docs for registering
custom providers](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations).

This project also comes with a [Dockerfile](./Dockerfile) for building a custom Docker image that already integrates
the JAR artifact within Keycloak. You may want to adapt the Dockerfile to your production setup. Build the image with
`docker build -t 52north/keycloak:latest`. To start a Keycloak instance from it use the provided
[Docker Compose setup](./docker/docker-compose.yml). 

## Credits
This project is inspired by the great https://github.com/evosec/keycloak-ipaddress-authenticator project. If you look
for conditional authentication flows based on the client's IP, you are right there.