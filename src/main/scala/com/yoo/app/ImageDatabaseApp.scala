package com.yoo.app

import java.util.concurrent.Executors

import com.yoo.app.controller.DatabaseController
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatra._

import scala.concurrent.{ExecutionContext, Future}

class ImageDatabaseApp(collection: MongoCollection[Document])
    extends ScalatraServlet
    with FutureSupport {

  override protected implicit def executor: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  private[this] val databaseController: DatabaseController = new DatabaseController(collection)(
    executor)

  /** Return the names of all the image files we've persisted so far
    */
  get("/") {
    new AsyncResult() {
      override val is: Future[_] = databaseController.getImageNames()
    }
  }

  delete("/images/:id") {
    new AsyncResult() {
      val toDelete = params("id")
      override val is: Future[_] = databaseController.deleteImage(toDelete).map {
        case Left(value) => NotFound(s"${value.reason}")
        case Right(value) => Ok(value)
      }
    }
  }
}
