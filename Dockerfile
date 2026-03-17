
# ─────────────────────────────────────────────────────────────
# Stage 1 — Test
#
# Equivalent to running manually:
#   mvn clean compile test
#
#   clean   → deletes the target/ folder (removes stale artifacts)
#   compile → compiles src/main/java/**/*.java into .class files
#   test    → compiles src/test/java/**/*.java and runs all
#             @Test methods (OrderWorkerTest, ProcessPayloadsTest,
#             SecurityConfigTest)
#
# NOTE: If any test fails, the Docker build stops here and does
# NOT proceed to packaging or running the app. This ensures a
# broken build is never deployed.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS tester

WORKDIR /app

# Copy Maven wrapper and POM first.
# Docker caches this layer — dependencies are only re-downloaded
# when pom.xml changes, keeping subsequent builds fast.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Pre-download all dependencies including test-scoped ones
# (equivalent to mvn dependency:go-offline)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy full source (main + test)
COPY src ./src

# Run: mvn clean compile test -B
#   clean   → removes target/ from any previous build
#   compile → compiles src/main/java/**/*.java
#   test    → compiles and runs all unit tests under src/test/java/
#   -B      → batch/non-interactive mode (no colour output)
RUN ./mvnw clean compile test -B


# ─────────────────────────────────────────────────────────────
# Stage 2 — Build
#
# Equivalent to running manually:
#   mvn package -DskipTests
#
# Tests already passed in Stage 1 so we skip them here to
# avoid running them twice. This stage only produces the JAR.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source AND the compiled test-verified classes from Stage 1
COPY src ./src
COPY --from=tester /app/target ./target

# Run: mvn package -DskipTests -B
#   package      → bundles compiled classes into a fat executable JAR
#   -DskipTests  → tests already ran and passed in Stage 1
#   -B           → batch mode
RUN ./mvnw package -DskipTests -B


# ─────────────────────────────────────────────────────────────
# Stage 3 — Runtime
#
# Equivalent to running manually:
#   java -XX:+UseContainerSupport \
#        -XX:MaxRAMPercentage=75.0 \
#        -Djava.security.egd=file:/dev/./urandom \
#        -jar target/OrderManager-0.0.1-SNAPSHOT.jar
#
# Only the JRE (not JDK) is needed here — keeps the image smaller.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the fat JAR produced in Stage 2 — nothing else
COPY --from=builder /app/target/*.jar app.jar

# Give ownership of the JAR to the non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the port defined in application.yaml (server.port = 9090)
EXPOSE 9090

# Run the JAR — equivalent to: java [flags] -jar app.jar
#
# JVM flags explained:
#   -XX:+UseContainerSupport     honours Docker CPU/memory limits
#   -XX:MaxRAMPercentage=75.0    heap = 75% of container memory
#   -Djava.security.egd=...      faster startup (skips /dev/random blocking)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
