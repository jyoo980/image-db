package com.yoo.app.model

sealed trait FileExtension {
  def toString: String
}

final case class PNG() extends FileExtension {
  override def toString: String = "png"
}

final case class JPEG() extends FileExtension {
  override def toString: String = "jpeg"
}

final case class GIF() extends FileExtension {
  override def toString: String = "gif"
}

object FileExtension {

  def apply(fileName: String): FileExtension = {
    val lastDot = fileName.lastIndexOf(".")
    fileName.substring(lastDot + 1, fileName.length) match {
      case "png" => PNG()
      case "jpeg" | "jpg" => JPEG()
      case "gif" => GIF()
    }
  }
}
