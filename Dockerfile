# OpenJDK 17 이미지를 기반으로 빌드
FROM eclipse-temurin:17

# .env 파일 복사
COPY .env /app/.env
# JAR 파일 경로 설정
ARG JAR_FILE=build/libs/stackpot-0.0.1-SNAPSHOT.jar

# JAR 파일을 컨테이너에 복사
COPY ${JAR_FILE} app.jar

# 애플리케이션 실행 시 .env 파일 로드
ENTRYPOINT ["sh", "-c", "source /app/.env && java -jar /app.jar"]
# 애플리케이션 실행
#ENTRYPOINT ["java", "-jar", "/app.jar"]
