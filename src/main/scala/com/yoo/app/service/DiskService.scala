package com.yoo.app.service

import java.io.{BufferedOutputStream, File, FileOutputStream, InputStream}

import com.yoo.app.model.{DiskError, WriteError}

class DiskService {

  def writeToDisk(id: String, stream: InputStream): Either[DiskError, String] = {
    val lazyStream = LazyList.continually(stream.read).takeWhile(_ != -1).map(_.toByte)
    val pathOnDisk = s"./$id"
    val file = new File(pathOnDisk)
    val target = new BufferedOutputStream(new FileOutputStream(file))
    try {
      lazyStream.foreach(target.write(_))
      Right(pathOnDisk)
    } catch {
      case e: Exception =>
        Left(WriteError(s"Failed to write: $id to disk with error: ${e.getMessage}"))
    } finally target.close()
  }

}
