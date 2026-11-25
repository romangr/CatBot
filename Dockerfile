# --- Build: compile a native executable with GraalVM -------------------------
FROM ghcr.io/graalvm/native-image-community:25 AS build

WORKDIR /src

# Resolve Gradle and dependencies first so they are cached between builds.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null

COPY src src
ARG GIT_REVISION="[UNKNOWN]"
RUN GIT_REVISION="${GIT_REVISION}" ./gradlew --no-daemon nativeCompile \
    && mkdir -p /out/data \
    && cp build/native/nativeCompile/catbot /out/catbot

# --- Runtime: glibc only, no JVM ---------------------------------------------
# The binary is static except for glibc (--static-nolibc), which the SQLite JNI
# library needs at runtime. It is extracted to /tmp on startup.
# Runs as uid 65532. Existing volumes created by older (root) images need a one-time
#   docker run --rm -v catbot-data:/data busybox chown -R 65532:65532 /data
FROM gcr.io/distroless/base-debian12:nonroot

COPY --from=build /out/catbot /catbot
COPY --from=build --chown=65532:65532 /out/data /data

# Default data paths (data/...) are relative to the working directory.
WORKDIR /
ENTRYPOINT ["/catbot"]
