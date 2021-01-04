package com.yoo.app.model

sealed trait DiskError {
  def reason: String
}

final case class WriteError(reason: String) extends DiskError

final case class DiskDeleteError(reason: String) extends DiskError
