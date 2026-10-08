# ---- build: compile, run unit tests, package WAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package

# ---- run
FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/sevatrack.war /usr/local/tomcat/webapps/ROOT.war
ENV TZ=Asia/Kolkata \
    JAVA_OPTS="-Duser.timezone=Asia/Kolkata"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s CMD curl -fs http://localhost:8080/ >/dev/null || exit 1
