package com.yoo.app.model

final case class Metadata(name: String, author: String, size: Long, ext: String, location: String)

final case class Image(name: String, content: String, meta: Metadata)
