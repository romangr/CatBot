FROM openjdk:14-alpine

ADD ./build/libs/CatBot-2.0-SNAPSHOT.jar ./app.jar

RUN mkdir data

CMD ["java", "-Xmx50m", "-Xms15m", "-jar", "app.jar"]
