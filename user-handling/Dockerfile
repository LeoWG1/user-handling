FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/user-handling-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9595
ENTRYPOINT ["java","-jar","app.jar"]