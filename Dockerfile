# Java 17이 설치된 베이스 이미지 사용
FROM eclipse-temurin:17-jdk-focal

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# JAR 파일을 컨테이너 내부에 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 헬스체크를 위한 포트 열기
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]


