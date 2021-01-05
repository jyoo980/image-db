package com.yoo.app

import org.scalatra.{AsyncResult, ScalatraContext}

import scala.concurrent.Future

object ServiceResponse {

  def apply(res: Future[_])(implicit ctxt: ScalatraContext): AsyncResult =
    new AsyncResult() {
      override val is: Future[_] = res
    }
}
