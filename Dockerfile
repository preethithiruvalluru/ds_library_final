# Builds the portal when the hosting service looks for a Dockerfile at the TOP of the repository.
# (It does the same as ds-library/Dockerfile, which is used instead when the host's "Root Directory" is set to ds-library.)

# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY ds-library/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY ds-library/src ./src
RUN mvn -q -B -DskipTests package

# ---- Stage 2: small runtime image, runs as a normal (non-root) user ----
FROM eclipse-temurin:17-jre
RUN useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY --from=build /build/target/ds-library-*.jar app.jar
USER app
EXPOSE 8085
# Small heap so it fits on a 512 MB free host. Change JAVA_OPTS if your host has more memory.
ENV JAVA_OPTS="-Xmx300m -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
