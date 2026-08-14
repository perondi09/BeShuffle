FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends wget ca-certificates && rm -rf /var/lib/apt/lists/* && \
    groupadd --system spring && useradd --system --gid spring --create-home --home-dir /home/spring spring && \
    mkdir -p /tmp && chown -R spring:spring /tmp

COPY --from=build /workspace/target/*.jar /app/app.jar

ENV TZ=UTC \
    SERVER_PORT=8080 \
    LOG_PATH=/tmp

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["sh", "-c", "exec java -DLOG_PATH=${LOG_PATH:-/tmp} -Dserver.port=${SERVER_PORT:-8080} -jar /app/app.jar"]