FROM openjdk:8-jre-alpine
WORKDIR /app
COPY build/libs .
RUN ls /app
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-cp", "*", "boston.convertdata.Application"]
