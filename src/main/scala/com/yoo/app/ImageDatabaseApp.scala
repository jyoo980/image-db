package com.yoo.app

import java.util.concurrent.Executors

import com.yoo.app.config.ImageDatabaseConfig
import com.yoo.app.dao.ImageDAO
import com.yoo.app.service.DiskService
import io.circe.generic.auto._, io.circe.syntax._
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}

import scala.concurrent.{ExecutionContext, Future}

class ImageDatabaseApp(collection: MongoCollection[Document])
    extends ScalatraServlet
    with FutureSupport
    with FileUploadSupport {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(ImageDatabaseConfig.maxUploadSize)))

  override protected implicit def executor: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  private[this] val imageDao: ImageDAO = new ImageDAO(collection)(executor)
  private[this] val disk: DiskService = new DiskService

  /** Return the names of all the image files we've persisted so far.
    */
  get("/") {
    new AsyncResult() {
      override val is: Future[_] = imageDao.getImageNames().map(Ok(_))
    }
  }

  /** Return the names of all the images that are associated with the given author.
    */
  get("/images/:author") {
    val author = params("author")
    new AsyncResult() {
      override val is: Future[_] = imageDao.getImagesByAuthor(author).map(Ok(_))
    }
  }

  /** Returns the metadata associated with the given author's images stored in the service.
    */
  get("/images/metadata/author/:author") {
    val author = params("author")
    new AsyncResult() {
      override val is: Future[_] = imageDao.getImageMetadataByAuthor(author).map {
        case Left(error) => InternalServerError(error.reason)
        case Right(value) => Ok(value.map(_.asJson).asJson)
      }
    }
  }

  /** Return the metadata of an image, given its id.
    */
  get("/images/metadata/:id") {
    val fileName = params("id")
    new AsyncResult() {
      override val is: Future[_] = imageDao.getImageMetadata(fileName).map {
        case Left(error) => InternalServerError(error.reason)
        case Right(value) => Ok(value.asJson)
      }
    }
  }

  /** Upload an image to the service.
    */
  post("/images/:author/:id") {
    val image = request.getPart("image")
    val id = params.as[String]("id")
    val author = params.as[String]("author")
    val size = image.getSize
    val imageStream = image.getInputStream
    disk.writeToDisk(id, imageStream) match {
      case Left(error) => InternalServerError(error.reason)
      case Right(value) =>
        new AsyncResult() {
          override val is: Future[_] = imageDao.saveImage(id, author, size, value).map {
            case Left(error) => InternalServerError(error.reason)
            case Right(value) => Ok(value)
          }
        }
    }
  }

  /** Delete an image from the service.
    */
  delete("/images/:id") {
    val toDelete = params.as[String]("id")
    disk.deleteFromDisk(toDelete) match {
      case Left(error) => InternalServerError(error.reason)
      case Right(_) =>
        new AsyncResult() {
          override val is: Future[_] = imageDao.deleteImage(toDelete).map {
            case Left(error) => NotFound(error.reason)
            case Right(value) => Ok(value)
          }
        }
    }
  }

  /** Bulk delete the images of the given author
    */
  delete("/images/:author") {
    val authorToDelete = params.as[String]("author")
    new AsyncResult() {
      override val is: Future[_] = imageDao.deleteImagesByAuthor(authorToDelete).map {
        case Left(error) => InternalServerError(error.reason)
        case Right(value) =>
          disk.bulkDeleteFromDisk(value) match {
            case Left(error) => InternalServerError(error.reason)
            case Right(result) => Ok(result)
          }
      }
    }
  }

}
