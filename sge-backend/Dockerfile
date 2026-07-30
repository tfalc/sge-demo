# Build context: repositório raiz (school_crm)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY sge-backend/pom.xml sge-backend/pom.xml
COPY sge-backend/src sge-backend/src
COPY schools/escola-demo schools/escola-demo
WORKDIR /build/sge-backend
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /build/sge-backend/target/sge-backend-*.jar app.jar
COPY sge-backend/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
