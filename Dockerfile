FROM openjdk:17.0.2-slim

# Add a non-root user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY target/smucode-0.0.1-SNAPSHOT.jar smucode-user-service.jar

# Set ownership to the non-root user
RUN chown -R spring:spring /app

USER spring

EXPOSE 8080 8761

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "smucode-user-service.jar"]