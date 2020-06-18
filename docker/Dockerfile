FROM java:openjdk-8-jre-alpine

ARG ENV

RUN apk --no-cache add curl

RUN mkdir -p /data/lucene
RUN mkdir -p /data/ala-namematching-service/config

COPY ./src/main/resources/subgroups.json /data/ala-namematching-service/config/subgroups.json
COPY ./src/main/resources/groups.json /data/ala-namematching-service/config/groups.json
COPY ./target/ala-namematching-service-1.0-SNAPSHOT.jar /data/ala-namematching-service.jar
COPY ./config.yml /data/config.yml

RUN if [ "$ENV" = "gbif-backbone" ]; then \
    curl -sf  -o /data/lucene/namematching.tgz -L https://datos.gbif.es/others/namematching-gbif-backbone-2020-06-18.tgz; else \
    curl -sf  -o /data/lucene/namematching.tgz -L https://archives.ala.org.au/archives/nameindexes/latest/namematching-20200214.tgz; fi
#COPY namematching.tgz /data/lucene/namematching.tgz

RUN tar zxf /data/lucene/namematching.tgz -C /data/lucene
RUN if [ "$ENV" != "gbif-backbone" ]; then \
    mv /data/lucene/namematching-20200214 /data/lucene/namematching; fi

EXPOSE 9179
EXPOSE 9180

CMD ["java", "-jar", "/data/ala-namematching-service.jar", "server", "/data/config.yml"]
