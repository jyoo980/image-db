package com.yoo.app.dao

import java.io.InputStream

import com.yoo.app.model.{FileExtension, Image, Metadata}
import com.yoo.app.model.error.CollectionError

import scala.concurrent.{ExecutionContext, Future}

class ImageDAO(dataStore: DataStore)(implicit ec: ExecutionContext) {

  def getImageNames: Future[Seq[String]] = dataStore.getImageNames

  def getImagesByAuthor(author: String): Future[Seq[String]] = dataStore.getImagesByAuthor(author)

  def getImageMetadata(id: String): Future[Either[CollectionError, Metadata]] =
    dataStore.getImageMetadata(id)

  def getImage(id: String): Future[Either[CollectionError, Image]] =
    dataStore.getImage(id)

  def getImageMetadataByAuthor(author: String): Future[Either[CollectionError, Seq[Metadata]]] =
    dataStore.getImageMetadataByAuthor(author)

  def deleteImage(id: String): Future[Either[CollectionError, Long]] = dataStore.deleteImage(id)

  def deleteImagesByAuthor(author: String): Future[Either[CollectionError, Seq[String]]] =
    dataStore.deleteImagesByAuthor(author)

  def saveImage(
      id: String,
      author: String,
      size: Long,
      location: String,
      content: InputStream
  ): Future[Either[CollectionError, String]] = {
    val fileExt = FileExtension(id).fold("n/a")(_.toString)
    val metadata = Metadata(id, author, size, fileExt, location)
    dataStore.saveImage(metadata, content)
  }

}
