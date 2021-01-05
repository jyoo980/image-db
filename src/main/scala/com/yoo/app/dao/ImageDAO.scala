package com.yoo.app.dao

import com.yoo.app.model.{FileExtension, Metadata}
import com.yoo.app.model.error.CollectionError

import scala.concurrent.{ExecutionContext, Future}

class ImageDAO(dataStore: DataStore)(implicit ec: ExecutionContext) extends {

  def getImageNames: Future[Seq[String]] = dataStore.getImageNames

  def getImagesByAuthor(author: String): Future[Seq[String]] = dataStore.getImagesByAuthor(author)

  def getImageMetadata(id: String): Future[Either[CollectionError, Metadata]] =
    dataStore.getImageMetadata(id)

  def getImageMetadataByAuthor(author: String): Future[Either[CollectionError, Seq[Metadata]]] =
    dataStore.getImageMetadataByAuthor(author)

  def deleteImage(id: String): Future[Either[CollectionError, Long]] = dataStore.deleteImage(id)

  def deleteImagesByAuthor(author: String): Future[Either[CollectionError, Seq[String]]] =
    dataStore.deleteImagesByAuthor(author)

  def saveImage(
      id: String,
      author: String,
      size: Long,
      location: String
  ): Future[Either[CollectionError, String]] = {
    val fileExt = FileExtension(id).fold("n/a")(ext => ext.toString)
    val metadata = Metadata(id, author, size, fileExt, location)
    dataStore.saveImage(metadata)
  }

}
