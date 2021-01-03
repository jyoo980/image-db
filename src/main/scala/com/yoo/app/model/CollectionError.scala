package com.yoo.app.model

sealed trait CollectionError {
  def reason: String
}

final case class DeleteError(toDelete: String) extends CollectionError {
  override def reason: String = s"Error occurred while deleting: $toDelete"
}
