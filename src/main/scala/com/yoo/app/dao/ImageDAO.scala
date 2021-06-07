package com.yoo.app.dao

import cats.data.EitherT
import com.yoo.app.model.{FileExtension, Metadata}
import com.yoo.app.model.error.CollectionError

import scala.concurrent.{ExecutionContext, Future}

class ImageDAO(dataStore: DataStore)(implicit ec: ExecutionContext) {

  def getImageNames: Future[Seq[String]] = dataStore.getImageNames

  def getImagesByAuthor(author: String): Future[Seq[String]] = dataStore.getImagesByAuthor(author)

  def getImageMetadata(id: String): EitherT[Future, CollectionError, Seq[Metadata]] =
    dataStore.getImageMetadata(id)

  def getImageMetadataByAuthor(author: String): EitherT[Future, CollectionError, Seq[Metadata]] =
    dataStore.getImageMetadataByAuthor(author)

  def deleteImage(id: String, author: String): EitherT[Future, CollectionError, Long] =
    dataStore.deleteImage(id, author)

  def deleteImagesByAuthor(author: String): EitherT[Future, CollectionError, Seq[String]] =
    dataStore.deleteImagesByAuthor(author)

  def saveImage(
      id: String,
      author: String,
      size: Long,
      location: String
  ): EitherT[Future, CollectionError, String] = {
    val fileExt = FileExtension(id).fold("n/a")(ext => ext.toString)
    val metadata = Metadata(id, author, size, fileExt, location)
    dataStore.saveImage(metadata)
  }

}
