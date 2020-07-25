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

#### Hinting

Search requests may contain hints.
These are lists of possible values for un-specified elements of the search.
An example search with hints is:

```json
{
  "scientificName": "Acacia dealbata",
  "family": "Fabaceae",
  "hints": {
    "kingdom": [ "Plantae", "Fungi" ],
    "family": [ "Fabaceae", "Chenopodiaceae" ]
  }
}
```

Hints are used in two ways, if the server is configured to use them - see [below](#Configuration)

* Hints are used to fill out the search if the corresponding term is absent in the search.
  In the above example, the service will try and match against a copy of the term where
  the kingdom is null, Plantae and Fungi.
  The family hint is not used, since it has been supplied.
  Searches proceed from least specific match using the least number of hints (none) to
  the most specific match using the largest number of hints, stopping when something is found.
* Hints are also used to sanity-check the resulting match.
  If hints are available, then the resulting match is checked against the list of hints and
  flagged with a `hintMismatch` issue if the match does not correspond to the hint.


### Health Check

To see your applications health enter url `http://localhost:9180/healthcheck`

### Test

`http://localhost:9179/search?q=macropus+rufus`

### Configuration

The name matching service uses a YAML configuration file with a number of possible entries.
Most of these entries have suitable defaults.

| | | | Description | Example | Default |
| --- | --- | --- | --- | --- | --- |
| logging | | | Logging configuration, see https://www.dropwizard.io/en/latest/manual/configuration.html for documentation | | |
| server | | | Server configuration, see https://www.dropwizard.io/en/latest/manual/configuration.html for documentation | | |
| search | | | Search configuration | | |
| | index | | The path of the index directory | | `/data/lucene/namematching` |
| | groups | | URL of the groups configuration |  | `file:///data/ala-namematching-service/config/groups.json` | 
| | subgroups | | URL of the subgroups configuration |  | `file:///data/ala-namematching-service/config/subgroups.json` | 
| | useHints | | Use hints supplied by the request to aid matching | | true |
| | checkHints | | Check the resulting match against the supplied hints as a sanity check | | true |

The `groups.json` file is a list of common names for taxa, eg.

```json
[
...
{
    "name": "Molluscs",
    "rank": "phylum",
    "included": ["Mollusca"],
    "excluded": [],
    "parent": "Animals"
  }
...
]
```

Where `name` is the descriptive group name, usually a common name, `rank` is the rank of the associated taxa,
`included` and `excluded` provide lists of taxonomic names for the group
and `parent` is the name of immediate parent group.
The taxa are matched against the taxonomic index, specified by `search.index` in the configuration.

The `subgroups.json` file is a list of further, more detailed descriptions for taxa.
The subgroups are attached to parent groups. Eg.

```json
[
...
  {
    "speciesGroup": "Birds",
    "taxonRank": "order",
    "taxa": [
      {
        "name": "ANSERIFORMES",
        "common": "Ducks, Geese, Swans"
      },
      {
        "name": "APODIFORMES",
        "common": "Hummingbirds, Swifts"
      },
      ...
    ]
   },
...
]
```

The `speciesGroup` refers to the group name, from the file above, the `taxonRank` gives the rank of the names
in the list and `taxa` is a list of mappings from scientific names to common descriptions.
As with the groups list, the subgroups are matched using the current name index.

## Building the docker image

Change directory to the `server` module.

```shell script
docker build -f docker/Dockerfile . -t ala-namematching-service:v20200722-1
```
for use ALA namematching and for use the GBIF backbone:

```shell script
docker build -f docker/Dockerfile . -t  ala-namematching-service:v20200722-1 --build-arg ENV=gbif-backbone
```