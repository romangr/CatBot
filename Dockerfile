FROM openjdk:11.0-slim

ADD . ./build

WORKDIR ./build

RUN ./gradlew jar --no-daemon

ADD ./build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

WORKDIR ..

CMD ["rm", "-rf", "build"]

CMD ["java", "-jar", "app.jar"]
