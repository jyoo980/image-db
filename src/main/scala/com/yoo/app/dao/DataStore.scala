package com.yoo.app.dao

import cats.data.EitherT
import com.yoo.app.model.Metadata
import com.yoo.app.model.error.CollectionError

import scala.concurrent.Future

trait DataStore {

  /** Gets the names of all images persisted on disk.
    * @return a sequence of strings which are image filenames.
    */
  def getImageNames: Future[Seq[String]]

  /** Gets the names of all images that are associated with an author.
    * @param author the author whose images we run a search for.
    * @return the images that belong to the author.
    */
  def getImagesByAuthor(author: String): Future[Seq[String]]

  /** Return the metadata of the image with the given filename.
    * @param id the filename of the image we want to obtain the metadata for.
    * @return either a CollectionError or the image's metadata.
    */
  def getImageMetadata(id: String): EitherT[Future, CollectionError, Seq[Metadata]]

  /** Return the metadata of all images associated with the given author.
    * @param author the author whose images we are fetching the metadata for.
    * @return either a CollectionError or a sequence of metadata for the given author's images.
    */
  def getImageMetadataByAuthor(author: String): EitherT[Future, CollectionError, Seq[Metadata]]

  /**  Deletes the image with the given id and author from the collection.
    * @param id the filename of the image we want to delete.
    * @param author the author to which the image we want to delete belongs.
    * @return either a CollectionError on failure, or the number of deleted documents.
    */
  def deleteImage(id: String, author: String): EitherT[Future, CollectionError, Long]

  /** Deletes the images associated with the given author from the collection.
    * @param author the author whose images we want to delete.
    * @return either a CollectionError on failure, or a sequence of images that have been deleted.
    */
  def deleteImagesByAuthor(author: String): EitherT[Future, CollectionError, Seq[String]]

  /** Persists image metadata to MongoDB after saving the image to disk.
    * @param imageMetadata the metadata of the image we want to save to disk.
    * @return either a CollectionError or the filename of the image whose metadata we saved to disk.
    */
  def saveImage(imageMetadata: Metadata): EitherT[Future, CollectionError, String]
}
