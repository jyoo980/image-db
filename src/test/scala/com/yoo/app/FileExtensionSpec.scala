package com.yoo.app

import com.yoo.app.model.{FileExtension, GIF, JPEG, PNG}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class FileExtensionSpec extends DefaultSpec {

  "apply" should "evaluate to the correct case class given a file extension" in {
    val extensions = List(".png", ".jpeg", ".jpg", ".gif")
    val classes = List(PNG(), JPEG(), JPEG(), GIF())

    extensions.zip(classes).foreach { case (fileExt, expectedExt) =>
      FileExtension(fileExt).map(_ shouldBe expectedExt)
    }
  }

  it should "evaluate to None given an extension that it does not recognize" in {
    val tiff = ".tiff"

    FileExtension(tiff) shouldBe None
  }

  it should "evaluate to None given a filename that is not well-formed" in {
    val malformedFileName = "barpng"

    FileExtension(malformedFileName) shouldBe None
  }
}
