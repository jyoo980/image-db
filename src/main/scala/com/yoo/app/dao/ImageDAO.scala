package com.yoo.app.dao

import com.yoo.app.model.{CollectionError, DeleteError, DuplicateWriteError}
import org.mongodb.scala.{DuplicateKeyException, MongoCollection, MongoException}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

class ImageDAO(collection: MongoCollection[Document])(implicit ec: ExecutionContext) {

  /** Gets the names of all images persisted on disk.
    * @return a sequence of strings which are image filenames.
    */
  def getImageNames(): Future[Seq[String]] =
    collection.find().toFuture().map { documents =>
      documents.flatMap(_.get("_id").map(_.asString().getValue))
    }

  /**  Deletes the image with the given filename from the collection.
    * @param id the filename of the image we want to delete.
    * @return either a CollectionError on failure, or the number of deleted documents.
    */
  def deleteImage(id: String): Future[Either[CollectionError, Long]] =
    collection.deleteOne(equal("_id", id)).toFuture().map { result =>
      if (result.getDeletedCount > 0) {
        Right(result.getDeletedCount)
      } else {
        Left(DeleteError(s"Image: $id does not exist."))
      }
    }

  /** Persists image metadata to MongoDB after saving the image to disk.
    * @param id the filename of the image whose metadata we want to persist.
    * @param author the author of the image.
    * @param size the size of the image in bytes.
    * @param location the location of the image in the filesystem.
    * @return either a CollectionError or the filename of the image whose metadata we saved to disk.
    */
  def saveImage(
      id: String,
      author: String,
      size: Long,
      location: String): Future[Either[CollectionError, String]] = {
    val toSave = Document("_id" -> id, "author" -> author, "size" -> size, "location" -> location)
    collection
      .insertOne(toSave)
      .toFuture()
      .map(_ => Right(id))
      .recover {
        case _: DuplicateKeyException => Left(DuplicateWriteError(s"Image: $id already exists."))
        case e: MongoException =>
          Left(DuplicateWriteError(s"Error while writing: $id, error: ${e.getMessage}"))
      }
  }

}
