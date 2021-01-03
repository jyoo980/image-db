import com.yoo.app.{ImageDatabaseApp, _}
import org.scalatra._
import javax.servlet.ServletContext
import org.mongodb.scala.MongoClient

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {
    val mongoClient = MongoClient()
    val mongoCollection = mongoClient.getDatabase("test").getCollection("testCollection")
    context.mount(new ImageDatabaseApp(mongoCollection), "/*")
  }
}
