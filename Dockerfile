ARG KC_IMAGE_VERSION="26.4.7"

FROM maven:3-eclipse-temurin-21-alpine AS mvnbuilder

COPY src src
COPY pom.xml pom.xml
RUN mvn clean package -Pdocker

FROM quay.io/keycloak/keycloak:${KC_IMAGE_VERSION} AS kcbuilder
COPY --from=mvnbuilder /target/keycloak-ip-authenticator.jar /opt/keycloak/providers/keycloak-ip-authenticator.jar

WORKDIR /opt/keycloak-extension
RUN cp -r /opt/keycloak/* .
RUN ls -lhat
RUN ./bin/kc.sh build

FROM quay.io/keycloak/keycloak:${KC_IMAGE_VERSION}
COPY --from=kcbuilder /opt/keycloak-extension/ /opt/keycloak/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]