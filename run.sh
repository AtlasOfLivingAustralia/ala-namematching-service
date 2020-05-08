#!/usr/bin/env bash
mvn package && java -jar target/ala-namematching-service-1.0-SNAPSHOT.jar server config.yml

