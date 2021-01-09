package com.yoo.app.model

import com.yoo.app.model.error.ImageDatabaseError

trait ResponseEncoder {

  def toResponseMap[A](res: A): Map[String, A] = Map("results" -> res)
  def toError[A <: ImageDatabaseError](err: A): Map[String, A] = Map("error" -> err)
}
