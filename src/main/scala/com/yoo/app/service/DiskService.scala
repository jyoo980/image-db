package com.yoo.app.service

import java.io.{BufferedOutputStream, File, FileOutputStream, InputStream}

import com.yoo.app.model.{DiskDeleteError, DiskError, WriteError}

class DiskService {

  /** Given the id (usually filename) of an image and its content as a stream, persist it to disk.
    * @param id the filename of the image we want to persist.
    * @param stream the content of the image as a stream.
    * @return either a DiskError or the path to which the image was written to on disk.
    */
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

  /** Given the id (usually filename) of an image, remove it from disk.
    * @param id the filename of the image we want to delete.
    * @return either a DiskError or the filename of the image that has been successfully deleted.
    */
  def deleteFromDisk(id: String): Either[DiskError, String] = {
    val pathOnDisk = s"./$id"
    try {
      new File(pathOnDisk).delete()
      Right(s"Successfully deleted: $id")
    } catch {
      case e: Exception =>
        Left(DiskDeleteError(s"Failed to delete: $id from disk with error: ${e.getMessage}"))
    }
  }

}
