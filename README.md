# ALA Name Matching Service

This is a very basic prototype written just to support a proof of concept with GBIF's pipelines data processing backend.

How to start the ALANameMatchingService application
---

1. Run `mvn clean install` to build your application
1. Download a pre-built name matching index (e.g https://archives.ala.org.au/archives/nameindexes/latest/namematching-20190213.tgz), and unpackage zip or tar to `/data/lucene/namematching`
1. Start application with `java -jar target/ala-name-matching-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:9180`
1. Test with `http://localhost:9179/api/search?q=Acacia`. The response should look similar to:


```json

{
    "success": true,
    "scientificName": "Acacia",
    "scientificNameAuthorship": "Mill.",
    "taxonConceptID": "http://id.biodiversity.org.au/node/apni/6719673",
    "rank": "genus",
    "rankID": 6000,
    "lft": 590410,
    "rgt": 593264,
    "matchType": "exactMatch",
    "kingdom": "Plantae",
    "kingdomID": "http://id.biodiversity.org.au/node/apni/9443092",
    "phylum": "Charophyta",
    "phylumID": "http://id.biodiversity.org.au/node/apni/9443091",
    "classs": "Equisetopsida",
    "classID": "http://id.biodiversity.org.au/node/apni/9443090",
    "order": "Fabales",
    "orderID": "http://id.biodiversity.org.au/node/apni/9443087",
    "family": "Fabaceae",
    "familyID": "http://id.biodiversity.org.au/node/apni/9443086",
    "genus": "Acacia",
    "genusID": "http://id.biodiversity.org.au/node/apni/6719673",
    "species": null,
    "speciesID": null,
    "vernacularName": "Acacia",
    "speciesGroup": [
        "Plants"
    ],
    "speciesSubgroup": []
}

```

Health Check
---

To see your applications health enter url `http://localhost:9180/healthcheck`

Test
---

`http://localhost:9179/search?q=macropus+rufus`


Building the docker image
---

`
docker build -f docker/Dockerfile . -t ala-namematching-service:v20200214-2
`

