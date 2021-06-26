# Bundestag Speech Search

Simple search engine for querying speeches from the [Bundestag](https://www.bundestag.de/).

## Get started

```bash
$ git clone https://github.com/htw-projekt-p2p-volltextsuche/fulltext-search
$ cd fulltext-search

$ sbt run
```

### Configure HTTP-Port

The HTTP port defaults to **8421**.

It can be configured at startup by passing it to `sbt run [PORT]`
or by setting the environment variable *HTTP_PORT*.

### Run tests

```bash
$ sbt test
```

----

## Retrieve Search Results

📁 [To the API-Doc's](http://link-to-api-docs.TODO)

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

Actually the queries can be filtered by *speaker* or *affiliation*.

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
