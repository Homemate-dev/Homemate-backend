FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# curl + netcat 설치 (헬스체크용)
RUN apk add --no-cache curl netcat-openbsd

# GitHub Actions에서 만든 jar를 복사
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
