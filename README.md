# ALA Name Matching Service [![Build Status](https://travis-ci.com/AtlasOfLivingAustralia/ala-namematching-service.svg?branch=master)](https://app.travis-ci.com/github/AtlasOfLivingAustralia/ala-namematching-service)

This priovides a set of web services for name matching, using the `ala-name-matching` library.
It consists of three components. all with maven groupId `au.org.ala.names`:

* `ala-namematching-core` A core library containing common objects
* `ala-namematching-client` A client library that can be linked into other applications and which accesses the web services
* `ala-namemacthing-server` A server application that can be used for name searches

## Client Library

To include the client library in an application include the following dependency

```xml
<dependency>
    <groupId>au.org.ala.names</groupId>
    <version>1.6-SNAPSHOT</version>
    <artifactId>ala-namematching-client</artifactId>
</dependency>
```

To access the client library, create a configuration and then create a client based on the configuration.
The client implements the [name matching API](client/src/main/java/au/org/ala/names/ws/api/NameMatchService.java).
You can do this either programmatically, using the client configuration builder:

```java
ClientConficonfiguration configuration = ClientConfiguration.builder()
    .baseUrl(new URL("https://namematching-ws.arg.au"))
    .timeOut(300000)
    .cacheSize(200000)
    .build();
this.client = new ALANameUsageMatchServiceClient(configuration);
```

The possible configuration parameters are

| parameter | default | description |
| --------- | ------- | ------------ |
| baseUrl | | The base URL of the name matching service |
| timeOut | 30000 | The connection timeout in milliseconds |
| cache | true | Cache server requests and responses (see below for *data* caching) |
| cacheDir |  | The cache directory (defaults to a temporary directory) |
| cacheSize | 52428800 (50Mb) | The cache size in bytes |

Or you can read a configuration from a json or YML document, via Jackson.
For example:

```json
{
  "baseUrl": "https://namematching-ws.arg.au",
  "timeOut": 3000,
  "cache": false
}
```
```java
ObjectMapper mapper = new ObjectMapper();
ClientConficonfiguration configuration = om.readValue(new File("config.json"), ClientCondifguration.class);
this.client = new ALANameUsageMatchServiceClient(configuration);
```

### Data caching

As well as a web service cache, the application can configure a *data cache* that holds
the results of name searches.
The data cache can be used to improve the performance of the `match(NameSearch)` and
`matchAll(List<NameSearch>)` calls by caching responses.
In the case of the `matchAll` call, partial matches result in a partial request to the
server, with the already cached items filled from the cache.

The client library has data caching disabled by default.
If you intend to use a sara cache, you will need to include an cache2k implementation
in your dependencies.
For example:

```xml
<dependency>   
  <groupId>org.cache2k</groupId>
  <artifactId>cache2k-jcache</artifactId>
  <version>1.2.0.Final</version>
</dependency>
```


To build a data-cached client, you need to build a data cache configuration and
add it to the client cofiguration.

```java
DataCacheConfiguration dataCache = DataCacheConfiguration.builder()
        .enableJmx(false)
        .build();
ClientConficonfiguration configuration = ClientConfiguration.builder()
        .baseUrl(new URL("https://namematching-ws.arg.au"))
        .dataCache(dataCache)
        .build();
this.client = new ALANameUsageMatchServiceClient(configuration);
```

or

```json
{
  "baseUrl": "https://namematching-ws.arg.au",
  "dataCache": {
    "enableJmx": false
  }
}
```

The possible data cache configuration parameters are:

| parameter | default | description |
| --------- | ------- | ------------ |
| enableJmx | true | Enable Java Management Extension monitoring of the cache. This allows a running applicationm to be queried about cache performance via applications such as `jconsole` |
| entryCapacity | 100000 | The number of entries to cache |
| eternal | true | If true, do not expire old entries |
| keepDataAfterExpired | false  | Keep data in the cache after expiry |
| permitNullValues | true | Allow caching of nulls |
| suppressExceptions | false | Suppress, rather than propagate exceptions |

## How to start the ALANameMatchingService application

1. Run `mvn clean install` to build your application
1. Download a pre-built name matching index (e.g https://archives.ala.org.au/archives/nameindexes/20210811/namematching-20210811.tgz), and untar in `/data/lucene` This will create a `/data/lucene/namematching-20210811` directory.
1. cd to the `server` subdirectory
1. Start application with `java -jar target/ala-name-matching-server-1.6-SNAPSHOT.jar server config-local.yml`
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
docker build -f docker/Dockerfile . -t ala-namematching-service:v20210811-2
```
for use ALA namematching and for use the GBIF backbone:

```shell script
docker build -f docker/Dockerfile . -t  ala-namematching-service:v20210811-2 --build-arg ENV=gbif-backbone
```

If you want a quick'n'easy docker instance for testing, use

```shell script
docker build -f docker/Dockerfile-test . -t ala-namematching-service:test
```
