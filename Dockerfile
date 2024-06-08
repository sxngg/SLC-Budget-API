FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build  target/event-manager-0.0.1-SNAPSHOT.jar event-manager-slc.jar
EXPOSE 8080
EXPOSE 443
ENTRYPOINT ["java", "-jar", "event-manager-slc.jar"]
