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
  get("/images/:author") {
    val author = params("author")
    val result = imageDao.getImagesByAuthor(author).map(toResponseMap)
    ServiceResponse(result.map(_.asJson).map(Ok(_)))
  }

  /** Returns the metadata associated with the given author's images stored in the service.
    */
  get("/images/metadata/author/:author") {
    val author = params("author")
    ServiceResponse(imageDao.getImageMetadataByAuthor(author).map {
      case Left(error) =>
        InternalServerError(toError(error).asJson)
      case Right(value) =>
        Ok(toResponseMap(value).asJson)
    })
  }

  /** Return the metadata of an image, given its id.
    */
  get("/images/metadata/:id") {
    val fileName = params("id")
    ServiceResponse(imageDao.getImageMetadata(fileName).map {
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(value) => Ok(toResponseMap(value).asJson)
    })
  }

  /** Upload an image to the service.
    */
  post("/images/:author/:id") {
    val image = request.getPart("image")
    val id = params.as[String]("id")
    val author = params.as[String]("author")
    val size = image.getSize
    val imageStream = image.getInputStream
    ServiceResponse(imageDao.saveImage(id, author, size, s"./$id", imageStream).map {
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(value) => Ok(toResponseMap(value).asJson)
    })
  }

  /** Delete an image from the service.
    */
  delete("/images/:author/:id") {
    val toDelete = params.as[String]("id")
    disk.deleteFromDisk(toDelete) match {
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(_) =>
        ServiceResponse(imageDao.deleteImage(toDelete).map {
          case Left(error) => NotFound(toError(error).asJson)
          case Right(value) => Ok(toResponseMap(value).asJson)
        })
    }
  }

  /** Bulk delete the images of the given author
    */
  delete("/images/:author") {
    val authorToDelete = params.as[String]("author")
    ServiceResponse(imageDao.deleteImagesByAuthor(authorToDelete).map {
      case Left(error) => InternalServerError(toError(error).asJson)
      case Right(value) =>
        disk.bulkDeleteFromDisk(value) match {
          case Left(error) => InternalServerError(toError(error).asJson)
          case Right(result) => Ok(toResponseMap(result).asJson)
        }
    })
  }

  /** Navigate to a very fun url
    */
  get("/fun") {
    val url = new URI(ImageDatabaseConfig.funAddr)
    Desktop.getDesktop.browse(url)
  }
}
