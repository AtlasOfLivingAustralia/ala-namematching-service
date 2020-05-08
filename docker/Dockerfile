FROM java:openjdk-8-jre-alpine

RUN apk --no-cache add curl

RUN mkdir -p /data/lucene
RUN mkdir -p /data/ala-namematching-service/config

COPY ./src/main/resources/subgroups.json /data/ala-namematching-service/config/subgroups.json
COPY ./src/main/resources/groups.json /data/ala-namematching-service/config/groups.json
COPY ./target/ala-namematching-service-1.0-SNAPSHOT.jar /data/ala-namematching-service.jar
COPY ./config.yml /data/config.yml

RUN curl -sf  -o /data/lucene/namematching.tgz -L https://archives.ala.org.au/archives/nameindexes/latest/namematching-20200214.tgz
#COPY namematching.tgz /data/lucene/namematching.tgz

RUN tar zxf /data/lucene/namematching.tgz -C /data/lucene
RUN mv /data/lucene/namematching-20200214 /data/lucene/namematching

EXPOSE 9179
EXPOSE 9180

CMD ["java", "-jar", "/data/ala-namematching-service.jar", "server", "/data/config.yml"]
