FROM jboss/keycloak:15.0.0

# 在docker镜像中创建目录
RUN mkdir -p /opt/jboss/keycloak/providers
RUN mkdir -p /opt/jboss/keycloak/themes/vika

COPY extensions/authenticators/basic-authenticator/target/basic-authenticator-*-jar-with-dependencies.jar /opt/jboss/keycloak/providers/
COPY extensions/authenticators/social-authenticator/target/social-authenticator-*-jar-with-dependencies.jar /opt/jboss/keycloak/providers/
COPY extensions/providers/vika-sheet-provider/target/vika-sheet-provider-*-with-dependencies.jar /opt/jboss/keycloak/standalone/deployments/
COPY extensions/providers/vika-jdbc-provider/target/vika-jdbc-provider-*-with-dependencies.jar /opt/jboss/keycloak/standalone/deployments/
COPY config/ /opt/jboss/keycloak/standalone/configuration/

COPY extensions/themes/src/main/resources/theme/vika/ /opt/jboss/keycloak/themes/vika/
COPY extensions/themes/src/main/resources/theme/keycloak/ /opt/jboss/keycloak/themes/keycloak/
