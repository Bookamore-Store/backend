# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests clean package

# Не alpine: Temurin не публікує musl-образи для aarch64, тому 17-jre-alpine існує
# лише під linux/amd64 і мультиплатформна збірка на ньому падає. DEV живе на
# Ampere A1 (aarch64), тож runtime-база має бути glibc-ва. Вендор і мажорна версія
# ті самі — змінюється тільки базова ОС; образ важчає приблизно на 90 MB.
FROM eclipse-temurin:17-jre-noble AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

# External volume for user-uploaded files
VOLUME ["/app/uploads"]

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

