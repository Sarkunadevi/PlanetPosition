FROM openjdk:21-jdk-slim

WORKDIR /planetposition

COPY target/planetposition-api-0.0.1-SNAPSHOT.jar planetposition.jar

EXPOSE 8080

CMD ["java", "-jar", "planetposition.jar"]