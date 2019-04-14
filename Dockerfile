FROM openjdk:11.0-slim

ADD . ./app

RUN ./app/gradlew jar

ADD ./app/build/libs/CatBot-2.0-SNAPSHOT.jar ../app.jar

CMD ["rm", "-rf", "app"]

CMD ["java", "-jar", "app.jar"]
