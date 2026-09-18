FROM eclipse-temurin:21.0.12_8-jre

WORKDIR /app

COPY target/*.jar customerservice.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","customerservice.jar"]