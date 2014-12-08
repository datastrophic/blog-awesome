package util

import java.util.UUID
import java.util.zip.CRC32

object IdGenerator {

  def generateUUID: String = UUID.randomUUID().toString

  def computeUID(providerId: String, userId: String): String = {
    val uid = s"$providerId$userId"
    val crc = new CRC32()
    crc.update(uid.getBytes())
    crc.getValue().toHexString
  }

}
