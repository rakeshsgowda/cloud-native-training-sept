FROM eclipse-temurin:21.0.12_8-jre

WORKDIR /app

COPY target/*.jar accountservice.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","accountservice.jar"]