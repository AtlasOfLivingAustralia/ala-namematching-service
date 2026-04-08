FROM eclipse-temurin:8-jre

RUN mkdir -p /data/lucene /data/ala-namematching-service/config

# Jar is pre-built by the CI pipeline (mvn package) and renamed to a fixed name
COPY server/target/ala-namematching-server.jar /data/ala-namematching-server.jar

EXPOSE 9179 9180

ENTRYPOINT ["java"]
CMD ["-jar", "/data/ala-namematching-server.jar", "server", "/data/config.yml"]
