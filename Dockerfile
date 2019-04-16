FROM openjdk:11.0-slim

ADD . ./app

WORKDIR ./app

RUN ./gradlew jar -q

RUN cp ./build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

WORKDIR ..

CMD ["rm", "-rf", "app"]

RUN mkdir data

CMD ["java", "-Xmx50m", "-Xms15m", "-jar", "app.jar"]
