# syntax=docker/dockerfile:1

########################
# 1) Build stage
########################
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# 의존성 캐시가 최대한 살아있도록, 먼저 래퍼/그레이들 설정만 복사
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew
# 의존성 미리 받아서 캐시 워밍업 (소스 없음 → 그래도 resolve 됨)
RUN ./gradlew --no-daemon dependencies || true

# 이제 소스 복사 후 빌드
COPY src/ src/
RUN ./gradlew --no-daemon clean bootJar -x test

# 런타임 스테이지로 넘길 때 JAR 이름 고정
RUN bash -lc 'JAR=$(ls build/libs | grep -E ".+\\.jar$" | grep -v "plain" | head -n 1) && mv build/libs/$JAR app.jar'

########################
# 2) Runtime stage
########################
FROM openjdk:17-jdk-slim
WORKDIR /app

# 필요시 타임존 사용 시 주석 해제
# ENV TZ=Asia/Seoul

# 빌드 산출물 복사
COPY --from=builder /app/app.jar /app/app.jar

# 컨테이너 메모리 환경에 맞춘 JVM 튜닝(필요시 조정)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=10.0 -Dfile.encoding=UTF-8"

# Railway가 할당하는 $PORT를 Spring Boot에 명시적으로 바인딩
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT:-8080}"]
