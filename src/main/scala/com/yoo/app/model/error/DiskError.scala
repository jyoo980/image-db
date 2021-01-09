package com.yoo.app.model.error

sealed trait DiskError extends ImageDatabaseError

final case class WriteError(reason: String) extends DiskError

final case class DiskDeleteError(reason: String) extends DiskError
