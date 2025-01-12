FROM amazoncorretto:8 AS builder

WORKDIR /app
COPY ./. .
RUN ./gradlew build -x test

FROM amazoncorretto:8

ARG BUILD_VERSION
ENV BUILD_VERSION ${BUILD_VERSION}

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 9091
EXPOSE 9092
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
