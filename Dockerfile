#FROM ubuntu:latest AS build
#RUN apt-get update
#RUN apt-get install openjdk:17-jdk-alpine
FROM openjdk:17-jdk-alpine
COPY target/event-manager-0.0.1-SNAPSHOT.jar event-manager-slc.jar
ENTRYPOINT ["java", "-jar", "event-manager-slc.jar"]