FROM openjdk:11.0-slim

ADD . ./app

WORKDIR ./app

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git

RUN ./gradlew jar

RUN cp ./build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

WORKDIR ..

RUN rm -rf app

RUN mkdir data

CMD ["java", "-Xmx50m", "-Xms15m", "-jar", "app.jar"]
