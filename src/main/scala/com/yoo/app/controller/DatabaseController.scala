package com.yoo.app.controller

import com.yoo.app.model.{CollectionError, DeleteError}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

class DatabaseController(collection: MongoCollection[Document])(implicit ec: ExecutionContext) {

  /** Gets the names of all images persisted on disk.
    * @return a sequence of strings which are image filenames.
    */
  def getImageNames(): Future[Seq[String]] =
    collection.find().toFuture().map { documents =>
      documents.flatMap(_.values.map(value => value.asString.getValue))
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
        Left(DeleteError(id))
      }
    }
}
