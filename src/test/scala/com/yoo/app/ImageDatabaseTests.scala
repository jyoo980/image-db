package com.yoo.app

import org.scalatra.test.scalatest._

class ImageDatabaseTests extends ScalatraFunSuite {

  addServlet(classOf[ImageDatabaseApp], "/*")

  test("GET / on ImageDatabase should return status 200") {
    get("/") {
      status should equal(200)
    }
  }

}
