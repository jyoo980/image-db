package com.yoo.app

import com.yoo.app.model.DocumentTransformer
import org.mongodb.scala.Document
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class DocumentTransformerSpec extends AnyFlatSpec with Matchers with DocumentTransformerFixture {

  "extractId" should "evaluate to None given a document with no _id key" in {
    val doc = Document("id" -> "test")
    extractId(doc).isEmpty shouldBe true
  }

  it should "evaluate to a Some given a document with a _id key" in {
    val doc = Document("_id" -> "meme.jpeg")
    extractId(doc).map(_ shouldBe "meme.jpeg")
  }

  "extractMetadata" should "extract the correct fields from a well-formed Document" in {
    val doc = Document("_id" -> "meme.png", "author" -> "yoo", "size" -> 696969420696969L)
    val metadata = extractMetadata(doc)

    metadata.name shouldBe "meme.png"
    metadata.author shouldBe "yoo"
    metadata.size shouldBe 696969420696969L
    metadata.ext shouldBe "png"
  }

  it should "set default values in the case where documents are malformed" in {
    val doc = Document("not" -> "valid")
    val metadata = extractMetadata(doc)

    metadata.name shouldBe "n/a"
    metadata.author shouldBe "n/a"
    metadata.size shouldBe 0L
    metadata.ext shouldBe "n/a"
  }
}

sealed trait DocumentTransformerFixture extends DocumentTransformer
