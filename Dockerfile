FROM adoptopenjdk/openjdk11:alpine-slim

ENV ENVIRONMENT=dev

WORKDIR /app/shared-id-endpoint

COPY ./target/shared-id-endpoint-*.jar /app/shared-id-endpoint/shared-id-endpoint.jar

RUN mkdir -p /app/shared-id-endpoint/conf /app/shared-id-endpoint/log /app/shared-id-endpoint/data

COPY ./src/main/resources/data/GeoLite2-Country.mmdb /app/shared-id-endpoint/data/GeoLite2-Country.mmdb
COPY ./src/main/resources/data/GeoLite2-City.mmdb /app/shared-id-endpoint/data/GeoLite2-City.mmdb

EXPOSE 80 8005

ENTRYPOINT ["java", "-Dspring.profiles.active=default,${ENVIRONMENT}", "-XX:+UseParallelGC", "-jar", "/app/shared-id-endpoint/shared-id-endpoint.jar"]