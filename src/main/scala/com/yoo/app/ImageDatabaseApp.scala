package com.yoo.app

import java.awt.Desktop
import java.net.URI
import com.yoo.app.config.ImageDatabaseConfig
import com.yoo.app.dao.ImageDAO
import com.yoo.app.model.ResponseEncoder
import com.yoo.app.service.DiskService
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}

import java.io.Serializable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ImageDatabaseApp(imageDao: ImageDAO, disk: DiskService)(implicit ec: ExecutionContextExecutor)
    extends ScalatraServlet
    with FutureSupport
    with FileUploadSupport
    with ResponseEncoder {

  configureMultipartHandling(MultipartConfig(maxFileSize = Some(ImageDatabaseConfig.maxUploadSize)))

  override protected implicit def executor: ExecutionContext = ec

  /** Return the names of all the image files we've persisted so far.
    */
  get("/") {
    val result = imageDao.getImageNames.map(toResponseMap)
    ServiceResponse(result.map(_.asJson).map(Ok(_)))
  }

  /** Return the names of all the images that are associated with the given author.
    */
  get("/images/author/:author") {
    val author = params("author")
    val result = imageDao.getImagesByAuthor(author).map(toResponseMap)
    ServiceResponse(result.map(_.asJson).map(Ok(_)))
  }

  /** Returns the metadata associated with the given author's images stored in the service.
    */
  get("/images/metadata/author/:author") {
    val author = params("author")
    val res = imageDao
      .getImageMetadataByAuthor(author)
      .leftMap(err => InternalServerError(toError(err).asJson))
      .map(value => Ok(toResponseMap(value).asJson))
      .value
    ServiceResponse(res)
  }

  /** Return the metadata of an image, given its id.
    */
  get("/images/metadata/:id") {
    val fileName = params("id")
    val res = imageDao
      .getImageMetadata(fileName)
      .leftMap(err => InternalServerError(toError(err).asJson))
      .map(value => Ok(toResponseMap(value).asJson))
      .value
    ServiceResponse(res)
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
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(value) =>
        ServiceResponse(
          imageDao
            .saveImage(id, author, size, value)
            .leftMap(err => InternalServerError(toError(err).asJson))
            .map(value => Ok(toResponseMap(value).asJson))
            .value
        )
    }
  }

  /** Delete an image from the service.
    */
  delete("/images/:author/:id") {
    val author = params.as[String]("author")
    val toDelete = params.as[String]("id")
    disk.deleteFromDisk(toDelete) match {
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(_) =>
        ServiceResponse(
          imageDao
            .deleteImage(toDelete, author)
            .leftMap(err => InternalServerError(toError(err).asJson))
            .map(value => Ok(toResponseMap(value).asJson))
            .value
        )
    }
  }

  /** Bulk delete the images of the given author
    */
  delete("/images/author/:author") {
    val authorToDelete = params.as[String]("author")
    ServiceResponse(
      imageDao
        .deleteImagesByAuthor(authorToDelete)
        .leftMap(err => InternalServerError(toError(err).asJson))
        .map { value =>
          disk.bulkDeleteFromDisk(value) match {
            case Left(error) => InternalServerError(toError(error).asJson)
            case Right(result) => Ok(toResponseMap(result).asJson)
          }
        }
        .value
    )
  }

  /** Navigate to a very fun url
    */
  get("/fun") {
    val url = new URI(ImageDatabaseConfig.funAddr)
    Desktop.getDesktop.browse(url)
  }
}
