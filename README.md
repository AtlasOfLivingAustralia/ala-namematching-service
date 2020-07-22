# ALA Name Matching Service [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/ala-namematching-service.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/ala-namematching-service)

This priovides a set of web services for name matching, using the `ala-name-matching` library.
It consists of three components. all with maven groupId `au.org.ala.names`:

* `ala-namematching-core` A core library containing common objects
* `ala-namematching-client` A client library that can be linked into other applications and which accesses the web services
* `ala-namemacthing-server` A server application that can be used for name searches

## How to start the ALANameMatchingService application


1. Run `mvn clean install` to build your application
1. Download a pre-built name matching index (e.g https://archives.ala.org.au/archives/nameindexes/latest/namematching-20200214.tgz), and untar in `/data/lucene` This will create a `/data/lucene/namematching-20200214` directory.
1. cd to the `server` subdirectory
1. Start application with `java -jar target/ala-name-matching-server-1.0-SNAPSHOT.jar server config-local.yml`
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
    "nameType": "SCIENTIFIC",
    "synonymType": null,
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
    "speciesSubgroup": [],
    "issues": [
       "noIssue"
    ]
}

```

### Web Services

To see complete documentation of the webservices available enter url `http://localhost:9179`

### Health Check


To see your applications health enter url `http://localhost:9180/healthcheck`

### Test

`http://localhost:9179/search?q=macropus+rufus`


## Building the docker image


Change directory to the `server` module.

`
docker build -f docker/Dockerfile . -t ala-namematching-service:v20200722-1
`
for use ALA namematching and for use the GBIF backbone:
`
docker build -f docker/Dockerfile . -t  ala-namematching-service:v20200722-1 --build-arg ENV=gbif-backbone
`