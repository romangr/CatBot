FROM openjdk:11.0-slim

ADD . ./app

WORKDIR ./app

RUN ./gradlew jar

ADD ./build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

WORKDIR ..

CMD ["rm", "-rf", "app"]

CMD ["java", "-jar", "app.jar"]
