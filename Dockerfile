FROM ubuntu:latest

RUN apt-get update && apt-get install -y openjdk-21-jdk

COPY target/smucode-user-service-0.0.1-SNAPSHOT.jar /app/smucode-user-service.jar

ENTRYPOINT ["java", "-jar", "/app/smucode-user-service.jar"]