#!/usr/bin/env bash
mvn package && java -jar target/ala-namematching-server-2.0-SNAPSHOT.jar server config.yml

