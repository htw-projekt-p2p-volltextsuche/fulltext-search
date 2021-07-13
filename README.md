# Bundestag Speech Search

Simple search engine for querying speeches from the [Bundestag](https://www.bundestag.de/).

## Get started

```bash
$ git clone https://github.com/htw-projekt-p2p-volltextsuche/fulltext-search
$ cd fulltext-search

$ sbt run
```

### Configure the App

The application configuration can be specified in *src/main/resources/application.conf*
in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation)
notation. It's also possible to override the *application.conf* by java system properties or environment variables.

Environment variables need to be prefixed by `CONFIG_FORCE_` except there is an *env-alias* specified for the property.

**They will be evaluated in following order** (starting from the highest priority):

1. Environment variable:               `export CONFIG_FORCE_SERVER_HOST="0.0.0.0"`
    - **This actually doesn't work!** - use env-alias if defined
2. Java system property as argument:   `sbt '; set javaOptions += "-Dserver.host="0.0.0.0""; run'`
3. *application.conf*:                 `server { host = 0.0.0.0 }`

#### Configuration properties

| identifier | description | env-alias | default |
|------------|------------:|----------:|--------:|
|server.port|HTTP port of the service|HTTP_PORT|8421|
|server.host|Host of the service|SERVER_HOST|0.0.0.0|
|server.log-body|Enables server logging of response bodies|SERVER_LOG_BODY|true|
|index.storage|Storage policy for the inverted index|INDEX_STORAGE_POLICY|local|
|index.stop-words-location|File name of the stopwords resource|-|stopwords_de.txt|
|index.sample-speeches-location|File name of the sample speeches resource|-|sample_speeches.json|
|index.insert-sample-speeches|Inserts sample speeches on startup when set|-|false|
|index.distribution-interval|Interval for scanning and distributing the cached index in ms|INDEX_DISTRIBUTION_INTERVAL|120000|
|index.distribution-chunk-size|Size of each concurrently processed chunk of the cached index|INDEX_DISTRIBUTION_CHUNK_SIZE|100|
|peers.uri|Entrypoint to the P2P network|-|http://localhost:8090/|
|peers.log-body|Enables client logging of response bodies|PEERS_LOG_BODY|true|

### Index Storage Policies

The application can be started with three different storage policies. They work as follows:

* `local`
  * Indexing and retrieval is both done locally in memory on the host machine.
* `distributed`
  * Indexing and retrieval is handled by calling the P2P network directly.
  * ⚠️ Using this option the index request blocks until all entries are distributed to the P2P network. This can possibly take a long while. Therefore, an appropriate timeout should be set on the calling machine.
* `lazy-distributed`
  * Indexing is done by first storing the index in a local cache and then distributing it on a background thread.
  * The interval for scanning the cached indexed can be configured with the option `index.distribution-interval`

### Run tests

```bash
$ sbt test
```

----

## Retrieve Search Results

📁 [To the API-Doc's](https://htw-projekt-p2p-volltextsuche.github.io/fulltext-search/)

### Simple Queries

Only searches with at least one term in the query fields are valid. All the other fields are optional.

To limit the maximum number of results `search.max_results` can be set to any positive integer.

The simplest possible search request has following form:

```json
{
  "search": {
    "query": {
      "terms": "your query here..."
    }
  }
}
 ```

In the above example all the terms specified in `terms` will be combined with *AND*.

To combine the terms with different boolean operators the search you can extend the search with arbitrary additional
terms.

```json
{
  "search": {
    "query": {
      "terms": "find this ...",
      "additions": [
        {
          "connector": "or",
          "terms": "... or that ..."
        },
        {
          "connector": "and_not",
          "terms": "... but not that"
        }
      ]
    }
  }
}
 ```

#### Evaluation order of boolean operators

1. First all `terms` fields are evaluated with *AND* in isolation.
1. The results will then be combined by the specified `connector` and evaluated in the following order:
    * *AND_NOT*
    * *AND*
    * *OR*

### Filtered Queries

Up until now the queries can be filtered by *speaker* or *affiliation*.

If several filters with same criteria are specified they're combined by *OR*, while the entire set resulting from all
filters of same type will by combined by *AND* with the actually specified query results.

```json
{
  "search": {
    "query": {
      "terms": "some search"
    },
    "filter": [
      {
        "criteria": "affiliation",
        "value": "SPD"
      },
      {
        "criteria": "affiliation",
        "value": "Die Linke"
      },
      {
        "criteria": "speaker",
        "value": "Peter Lustig"
      }
    ]
  }
}
 ```
