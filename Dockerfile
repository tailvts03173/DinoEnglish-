# Stage 1: Sử dụng nền tảng Java 25 chính thức và cài đặt Maven
FROM eclipse-temurin:25-jdk-alpine AS build
RUN apk add --no-cache maven

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng trên môi trường JRE siêu nhẹ
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
