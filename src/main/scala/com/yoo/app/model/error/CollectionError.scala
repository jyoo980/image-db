package com.yoo.app.model.error

sealed trait CollectionError {
  def reason: String
}

final case class DeleteError(reason: String) extends CollectionError

final case class DuplicateWriteError(reason: String) extends CollectionError

final case class MongoWriteError(reason: String) extends CollectionError

final case class LookupError(reason: String) extends CollectionError
