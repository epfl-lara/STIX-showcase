package filesystem.definitions

import FilesystemDefinitions._
import stainless.lang.StaticChecks._
import stainless.annotation._
import stainless.math.BitVectors._


object PartitionDefinitions {
  @cCode.export
  case class File_blockStatus_e(status: UInt8) {
    require(0 <= status && status <= 4)
  }

  @inline @cCode.define @cCode.export val E_FILE_BLOCK_STAT_UNKNOWN = File_blockStatus_e(0)
  @inline @cCode.define @cCode.export val E_FILE_BLOCK_STAT_FREE    = File_blockStatus_e(1)
  @inline @cCode.define @cCode.export val E_FILE_BLOCK_STAT_FILE    = File_blockStatus_e(2)
  @inline @cCode.define @cCode.export val E_FILE_BLOCK_STAT_ERROR   = File_blockStatus_e(3)
  @inline @cCode.define @cCode.export val E_FILE_BLOCK_STAT_BAD     = File_blockStatus_e(4)
}
