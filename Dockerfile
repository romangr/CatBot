FROM openjdk:11.0-slim

ADD . .

RUN ./gradlew jar --no-daemon

ADD ./build/libs/CatBot-2.0-SNAPSHOT.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
