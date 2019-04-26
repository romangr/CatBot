FROM openjdk:13-alpine

RUN apk update && apk upgrade && apk add --no-cache bash git openssh

ADD . ./app

WORKDIR ./app

RUN ./gradlew jar

RUN cp ./build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

WORKDIR ..

RUN rm -rf app

RUN mkdir data

CMD ["java", "-Xmx50m", "-Xms15m", "-jar", "app.jar"]
