#!/usr/bin/env bash
mvn package && java -jar target/ala-namematching-server-1.6.jar server config.yml

