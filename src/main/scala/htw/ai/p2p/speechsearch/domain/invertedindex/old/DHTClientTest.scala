package htw.ai.p2p.speechsearch.domain.invertedindex.old

import htw.ai.p2p.speechsearch.domain.model.speech.Posting

class DHTClientTest extends DHTClient {
  override def get(key: String): String =
    """{"error":false,"key":"test","value":[{"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
      |{"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}]}""".stripMargin

  override def getMany(terms: List[String]): String =
    if (terms == List("linux", "windows"))
      """{
        |  "error": false,
        |  "keys": [
        |    "linux",
        |    "windows"
        |  ],
        |  "values": {
        |      "linux": {
        |      "error": false,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    },
        |    "windows": {
        |      "error": true,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    }
        |  }
        |}""".stripMargin
    else if (terms == List("testHasResults", "testNoResults"))
      """{
        |  "error": false,
        |  "keys": [
        |    "testHasResults",
        |    "testNoResults"
        |  ],
        |  "values": {
        |      "testHasResults": {
        |      "error": false,
        |      "value": [
        |      {"doc_id":"b272829e-f15b-44fe-8c25-25e3eff45300","tf":1,"doc_len":100},
        |      {"doc_id":"32271a61-5c42-452f-8d59-7a2323c88bff","tf":2,"doc_len":10}
        |      ]
        |    },
        |    "testNoResults": {
        |      "error": true,
        |      "errorMsg" : "Not found"
        |    }
        |  }
        |}""".stripMargin
    else "{}"

  override def post(key: String, data: Posting): Unit = ???

  override def postMany(entries: Map[String, Posting]): Unit = ???
}
