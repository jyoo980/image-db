package com.yoo.app.dao

import com.yoo.app.model.{DocumentTransformer, Metadata}
import com.yoo.app.model.error.{CollectionError, DeleteError, DuplicateWriteError, LookupError}
import org.mongodb.scala.{DuplicateKeyException, MongoCollection, MongoException}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

class MongoStore(collection: MongoCollection[Document])(implicit ec: ExecutionContext)
    extends DataStore
    with DocumentTransformer {

  /** Gets the names of all images persisted on disk.
    * @return a sequence of strings which are image filenames.
    */
  override def getImageNames: Future[Seq[String]] =
    collection.find().toFuture().map(_.flatMap(extractId))

  /** Gets the names of all images that are associated with an author.
    * @param author the author whose images we run a search for.
    * @return the images that belong to the author.
    */
  override def getImagesByAuthor(author: String): Future[Seq[String]] =
    collection.find(equal("author", author)).toFuture().map(_.flatMap(extractId))

  /** Return the metadata of the image with the given filename.
    * @param id the filename of the image we want to obtain the metadata for.
    * @return either a CollectionError or the image's metadata.
    */
  override def getImageMetadata(id: String): Future[Either[CollectionError, Metadata]] =
    collection.find(equal("_id", id)).toFuture().map { documents =>
      documents.headOption
        .map(d => Right(extractMetadata(d)))
        .getOrElse(Left(LookupError(s"Image: $id does not exist")))
    }

  /** Return the metadata of all images associated with the given author.
    * @param author the author whose images we are fetching the metadata for.
    * @return either a CollectionError or a sequence of metadata for the given author's images.
    */
  override def getImageMetadataByAuthor(
      author: String): Future[Either[CollectionError, Seq[Metadata]]] =
    for {
      imagesByAuthor <- getImagesByAuthor(author)
      eitherMetadata <- Future.sequence(imagesByAuthor.map(getImageMetadata))
    } yield
      if (eitherMetadata.forall(_.isRight)) Right(eitherMetadata.flatMap(_.toSeq))
      else Left(LookupError(s"Error while looking up image metadata for author: $author"))

  /** Deletes the image with the given filename from the collection.
    * @param id the filename of the image we want to delete.
    * @return either a CollectionError on failure, or the number of deleted documents.
    */
  override def deleteImage(id: String): Future[Either[CollectionError, Long]] =
    collection.deleteOne(equal("_id", id)).toFuture().map { result =>
      if (result.getDeletedCount > 0) {
        Right(result.getDeletedCount)
      } else {
        Left(DeleteError(s"Image: $id does not exist."))
      }
    }

  /** Deletes the images associated with the given author from the collection.
    * @param author the author whose images we want to delete.
    * @return either a CollectionError on failure, or a sequence of images that have been deleted.
    */
  override def deleteImagesByAuthor(author: String): Future[Either[CollectionError, Seq[String]]] =
    for {
      imagesByAuthor <- getImagesByAuthor(author)
      eitherDeleteOps <- Future.sequence(imagesByAuthor.map(deleteImage))
    } yield
      if (eitherDeleteOps.forall(_.isRight)) Right(imagesByAuthor)
      else Left(DeleteError(s"Error while deleting images by author: $author"))

  /** Persists image metadata to MongoDB after saving the image to disk.
    * @param imageMetadata the metadata of the image we want to save to disk.
    * @return either a CollectionError or the filename of the image whose metadata we saved to disk.
    */
  override def saveImage(imageMetadata: Metadata): Future[Either[CollectionError, String]] = {
    val id = imageMetadata.name
    val docToSave = Document(
      "_id" -> id,
      "author" -> imageMetadata.author,
      "size" -> imageMetadata.size,
      "location" -> imageMetadata.location)
    collection
      .insertOne(docToSave)
      .toFuture()
      .map(_ => Right(id))
      .recover {
        case _: DuplicateKeyException =>
          Left(DuplicateWriteError(s"Image: $id already exists."))
        case e: MongoException =>
          Left(DuplicateWriteError(s"Error while writing: $id, error: ${e.getMessage}"))
      }
  }

}