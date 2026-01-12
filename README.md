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

## Dev Setup
For development and debugging purpose use the [docker-compose.dev.yml](./docker/docker-compose.dev.yml). First, run
`mvn clean package`, which creates a JAR artifact within the `./target` directory. Then start a Keycloak instance
with `docker compose -f ./docker/docker-compose.dev.yml up`. This injects the prebuilt JAR into the Keycloak container
and opens port `8787` for remote debugging.

## How to Use
### 1) Configure IP Authenticator
You can add the IP Authenticator as separate authentication step to one of the existing authentication flows within
the Keycloak Admin Console under *Authentication* -> *Flows*. Most probably you may want to provide as an authentication
option to the default browser flow. For this purpose, just duplicate the standard browser flow and add the IP
Authenticator as additional step with requirement *Alternative*. You can configure the Authenticator accordingly to your
Keycloak deployment setup. For instance, if Keycloak runs behind a proxy, enable the *Use a 'forwarded' header'* flag,
set the *Forwarded header name* and define the *Number of trusted proxies*. Finally, bind your custom flow to the 
*Browser flow* binding type.
### 2) Add Custom User Attributes
The IP Authenticator looks for dedicated user profile attributes in order to decide, which user should be authenticated.
These attributes are not part of the default user profile attributes, so you have to add them as custom attributes
at *Realm settings* -> *User profile*. Create the attributes as follows:
* *Attribute*: `ipAuthEnabled`
  * *General settings*
    * *Multivalued*: `Off`
    * *Enabled when*: `Always`
    * *Required field*: `On`
    * *Required for*: `Only users`
    * *Required when*: `Always`
  * *Permission*
    * *Who can edit?*: `Admin`
    * *Who can view?*: `Admin`
  * *Validations*
    * *Validator name*: `options`
    * *options*: `["true","false"]`
  * *Annotations*
    * *Key*: `inputType`
    * *Value*: `select`
* *Attribute*: `ipAuthRanges`
  * *General settings*
      * *Multivalued*: `On`
      * *Enabled when*: `Always`
      * *Required field*: `Off`
  * *Permission*
      * *Who can edit?*: `Admin`
      * *Who can view?*: `Admin`
* *Attribute*: `ipAuthMaxSession`
  * *General settings*
      * *Multivalued*: `Off`
      * *Enabled when*: `Always`
      * *Required field*: `Off`
  * *Permission*
      * *Who can edit?*: `Admin`
      * *Who can view?*: `Admin`
  * *Validations*
    * *Validator name*: `integer`
    * set min and max value for the parameter

### 3) Enable IP Authentication for Users
Finally, you have to enable authentication by IP address for some users by setting the `ipAuthEnabled` user profile 
attribute for those users to `true`. You also have to add one or more supported IP addresses and/or IP ranges for the
`ipAuthRanges` user profile attribute. Check [this documentation](https://seancfoley.github.io/IPAddress/ipaddress.html#ip-address-ranges)
to get more information about the supported IP range notations. These addresses/ranges are validated against a client's 
IP address, to choose which user should be authenticated.

### 4) Choose IP Authentication for Login
If you have properly configured IP Authentication as alternative step for the *Browser flow*, next time you have to
log in via Browser, you should see a *Try Another Way* button below the *Sign In* button. When you click on it, you
can choose IP Authentication as login method.

## Credits
This project is inspired by the great https://github.com/evosec/keycloak-ipaddress-authenticator project. If you look
for conditional authentication flows based on the client's IP, you are right there.