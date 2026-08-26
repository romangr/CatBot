FROM bellsoft/hardened-liberica-runtime-container:jre-21-musl

ADD ./build/libs/CatBot-2.0-SNAPSHOT.jar ./app.jar

RUN mkdir data

CMD ["java", "-Xmx50m", "-Xms15m", "-jar", "app.jar"]
