import java.util.concurrent.Executors

import com.yoo.app.dao.{DataStore, ImageDAO, MongoStore}
import com.yoo.app.service.DiskService
import com.yoo.app.{ImageDatabaseApp, _}
import org.scalatra._
import javax.servlet.ServletContext
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ScalatraBootstrap extends LifeCycle {

  implicit val ec: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(100))

  override def init(context: ServletContext) {
    val mongoClient = MongoClient()
    val collection: MongoCollection[Document] =
      mongoClient.getDatabase("test").getCollection("testCollection")
    val store: DataStore = new MongoStore(collection)
    val dao: ImageDAO = new ImageDAO(store)
    val disk: DiskService = new DiskService
    context.mount(new ImageDatabaseApp(dao, disk), "/*")
  }
}
