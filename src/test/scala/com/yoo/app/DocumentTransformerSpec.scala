package com.yoo.app

import com.yoo.app.model.{DocumentTransformer, FileExtension}
import org.mongodb.scala.Document
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class DocumentTransformerSpec extends DefaultSpec with DocumentTransformerFixture {

  "extractId" should "evaluate to None given a document with no _id key" in {
    val doc = Document("id" -> "test")
    extractId(doc) shouldBe None
  }

  it should "evaluate to a Some given a document with a _id key" in {
    val doc = Document("_id" -> "meme.jpeg")
    extractId(doc).map(_ shouldBe "meme.jpeg")
  }

  "extractExtension" should "evaluate to None given a document with no _id key" in {
    val doc = Document("notId" -> "shouldNotReturn")
    extractExtension(doc) shouldBe None
  }

  it should "evaluate to the correct file extension given valid extensions as string" in {
    val supportedExtensions = FileExtension.supported.map { case (ext, extType) =>
      (s"text.$ext", extType)
    }

    supportedExtensions.foreach { case (fileName, expectedExt) =>
      val doc = Document("_id" -> fileName)
      extractExtension(doc).map(_ shouldBe expectedExt)
    }
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
