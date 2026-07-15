# 借用一個已經裝好 Node.js 22 的輕量 Linux image 當起點。
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package*.json frontend/
RUN cd frontend && npm ci
COPY frontend/ frontend/
RUN cd frontend && npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY --from=frontend /app/src/main/resources/static/farmily-web ./src/main/resources/static/farmily-web
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /app/target/*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]