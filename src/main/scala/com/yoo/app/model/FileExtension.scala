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

  val supported = Map("png" -> PNG(), "jpeg" -> JPEG(), "jpg" -> JPEG(), "gif" -> GIF())

  def apply(fileName: String): Option[FileExtension] =
    fileName.split("\\.").toList match {
      case _ :: extension :: Nil => supported.get(extension)
      case _ => None
    }
}
