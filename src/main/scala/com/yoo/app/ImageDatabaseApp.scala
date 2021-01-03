package com.yoo.app

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.util.concurrent.Executors

import com.yoo.app.config.ImageDatabaseConfig
import com.yoo.app.dao.ImageDAO
import com.yoo.app.service.DiskService
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

  /** Return the names of all the image files we've persisted so far
    */
  get("/") {
    new AsyncResult() {
      override val is: Future[_] = imageDao.getImageNames().map(Ok(_))
    }
  }

  post("/images/:author/:id") {
    val image = request.getPart("image")
    val id = params.as[String]("id")
    val author = params.as[String]("author")
    val size = image.getSize
    val imageStream = image.getInputStream
    disk.writeToDisk(id, imageStream) match {
      case Left(error) => InternalServerError(error.reason)
      case Right(value) =>
        // TODO: probably make this an Either and use a for-comp
        imageDao.saveImage(id, author, size, value)
    }
  }

  delete("/images/:id") {
    new AsyncResult() {
      val toDelete = params.as[String]("id")
      override val is: Future[_] = imageDao.deleteImage(toDelete).map {
        case Left(value) => NotFound(s"${value.reason}")
        case Right(value) => Ok(value)
      }
    }
  }
}
