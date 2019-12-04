# ALANameMatchingService

This is a very basic prototype written just to support a proof of concept with GBIF's pipelines data processing backend.

How to start the ALANameMatchingService application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/ala-name-matching-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

Test
---

`http://localhost:9179/search?q=macropus+rufus`
