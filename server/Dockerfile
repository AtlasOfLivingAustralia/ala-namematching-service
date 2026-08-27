# Stage 1 — download and verify the Lucene name index at build time.
# Reuses the same pinned curl image as the former init container.
# The index layer is cached by Docker when INDEX_URL and INDEX_SHA1 are unchanged,
# so app-only rebuilds do not re-download the index.
FROM curlimages/curl:8.19.0 AS index-fetcher

ARG INDEX_URL
ARG INDEX_SHA1

RUN set -e \
    && mkdir -p /tmp/nm \
    && echo "Downloading index from $INDEX_URL ..." \
    && curl -fsSL --retry 3 --retry-delay 10 -o /tmp/namematching.tgz "$INDEX_URL" \
    && echo "Verifying SHA1..." \
    && echo "$INDEX_SHA1  /tmp/namematching.tgz" | sha1sum -c - \
    && echo "Extracting..." \
    && tar xf /tmp/namematching.tgz -C /tmp/nm --no-same-owner \
    && COUNT=$(ls /tmp/nm | wc -l) \
    && [ "$COUNT" -eq 1 ] || { echo "ERROR: expected 1 top-level directory in tarball, got $COUNT"; exit 1; } \
    && mv /tmp/nm/$(ls /tmp/nm) /tmp/namematching-index \
    && rm /tmp/namematching.tgz

# Stage 2 — runtime image.
FROM eclipse-temurin:8-jre

RUN mkdir -p /data/lucene /data/ala-namematching-service/config

# Copy the pre-extracted index from the build stage.
COPY --from=index-fetcher /tmp/namematching-index /data/lucene/namematching-nm

# Jar is pre-built by the CI pipeline (mvn package) and renamed to a fixed name
COPY server/target/ala-namematching-server.jar /data/ala-namematching-server.jar

# Default config so the image is runnable standalone (docker run/compose).
COPY server/config.yml /data/config.yml
COPY server/docker/groups.json /data/ala-namematching-service/config/groups.json
COPY server/docker/subgroups.json /data/ala-namematching-service/config/subgroups.json

EXPOSE 9179 9180

ENTRYPOINT ["java"]
CMD ["-jar", "/data/ala-namematching-server.jar", "server", "/data/config.yml"]
