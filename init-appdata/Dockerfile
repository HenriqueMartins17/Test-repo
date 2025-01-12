FROM amazoncorretto:17

ARG BUILD_VERSION
ENV BUILD_VERSION ${BUILD_VERSION}

WORKDIR /app

COPY build/libs/ /app/
COPY tmp/ /app/tmp/
COPY docker-entrypoint.sh .

ENTRYPOINT [ "./docker-entrypoint.sh"]

CMD ["init-user", "load", "init-config-space"]
