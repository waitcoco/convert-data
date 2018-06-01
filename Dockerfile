FROM openjdk:8-jre-alpine
WORKDIR /app
COPY build/libs .
RUN ls /app
ENTRYPOINT ["java", "-cp", "*", "boston.convertdata.Application"]