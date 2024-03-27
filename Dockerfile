FROM openjdk:17-jdk-slim
WORKDIR /app
COPY ./target/hs-gateway-0.0.1-SNAPSHOT.jar ./hs-gateway.jar
ENTRYPOINT ["java","-jar","/app/hs-gateway.jar"]