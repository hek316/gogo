# ── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# 의존성 캐시 레이어 (build 파일만 먼저 COPY)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# 소스 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Layered JAR 분리 (Docker 캐시 최적화)
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# 비루트 유저 (보안)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 레이어 순서 = Docker 캐시 효율 (변경 빈도 낮은 것부터)
COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
