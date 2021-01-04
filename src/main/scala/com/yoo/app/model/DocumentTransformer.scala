package com.yoo.app.model

import org.mongodb.scala.bson.collection.immutable.Document

trait DocumentTransformer {

  /** Given a document, return its id.
    * @param document the document we want to extract the id from.
    * @return the extracted id, if found.
    */
  def extractId(document: Document): Option[String] =
    document.get("_id").map(_.asString.getValue)

  /** Given a document, transform it to its metadata instance.
    * @param document the document for which we want to extract metadata for.
    * @return the metadata of the given document.
    */
  def extractMetadata(document: Document): Metadata = {
    val name = document.get("_id").map(_.asString.getValue).fold("")(id => id)
    val extension = FileExtension(name).toString
    Metadata(
      name = name,
      author = document.get("author").map(_.asString.getValue).fold("")(author => author),
      size = document.get("size").map(_.asInt64.getValue).fold(0L)(size => size),
      ext = extension
    )
  }

}
