/* Copyright 2021 Ateleris GmbH, Brugg */

package definitions

import stainless.math.BitVectors._
import stainless.annotation._
import stainless.lang.StaticChecks._

import scala.language.implicitConversions

object FilesystemDefinitions {

  /**
   * Information about the Flash Device itself
   */
  @inline @cCode.inline val FLASH_BLOCKS_PER_DEVICE: UInt32 = 4096 // quantity
  @inline @cCode.inline val FLASH_DEVICES_PER_MCM: UInt32 = 8 // quantity

  // Defines total number of blocks in a single flash chip
  @inline @cCode.inline val FLASH_BLOCKS_PER_MCM: UInt32 = (FLASH_DEVICES_PER_MCM * FLASH_BLOCKS_PER_DEVICE) // 32'768 blocks

  /**
   * Information about the Software Abstraction
   */
  // max amount of files per File Control Block
  @inline @cCode.inline val FILES_PER_FILECB: UInt32 = 60

  // (space for 2 ASW images that can be moved in case of bad blocks)
  @inline @cCode.inline val OFFSET_OF_FIRST_CONTROL_BLOCK_A: UInt32 = 64 //offset of 16MiB on FLASH A
  @inline @cCode.inline val OFFSET_OF_FIRST_CONTROL_BLOCK_B: UInt32 = 64 //offset of 16MiB on FLASH B

  // number of control blocks that abstract 60 files each in Flash Chip A
  @inline @cCode.inline val NUMBER_OF_CONTROL_BLOCKS_A = ((FLASH_BLOCKS_PER_MCM - OFFSET_OF_FIRST_CONTROL_BLOCK_A) / FILES_PER_FILECB) // 545 File CBs
  // number of control blocks that abstract 60 files each in Flash Chip B
  @inline @cCode.inline val NUMBER_OF_CONTROL_BLOCKS_B = ((FLASH_BLOCKS_PER_MCM - OFFSET_OF_FIRST_CONTROL_BLOCK_B) / FILES_PER_FILECB) // 545 File CBs
  // number of all control block (whole flash)
  @inline @cCode.inline val TOTAL_NUMBER_OF_CONTROL_BLOCKS = NUMBER_OF_CONTROL_BLOCKS_A + NUMBER_OF_CONTROL_BLOCKS_B // 1090 File CBs
  // theoretical maximum of files that can be written
  @inline @cCode.inline val MAX_NUMBER_OF_FILES: UInt32 = TOTAL_NUMBER_OF_CONTROL_BLOCKS * FILES_PER_FILECB // 65'400 Files


  @inline @cCode.inline val BITS_IN_INT32: UInt32 = 32

  @cCode.export
  case class MemFilesystem_filetype_e(partitionNumber: UInt8) {
    require(0 <= partitionNumber && partitionNumber <= 9)
  }

  @inline
  implicit def filetype_identity(fileType: MemFilesystem_filetype_e): UInt8 = fileType.partitionNumber

  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_FIRST = MemFilesystem_filetype_e(0)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_ASPECT = MemFilesystem_filetype_e(1)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_RAW_COUNTS = MemFilesystem_filetype_e(2)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_TMMGMT = MemFilesystem_filetype_e(3)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_FLARE = MemFilesystem_filetype_e(4)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_USER_REQUESTS= MemFilesystem_filetype_e(5)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_CONTEXT0 = MemFilesystem_filetype_e(6)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_CONTEXT1 = MemFilesystem_filetype_e(7)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_LUT = MemFilesystem_filetype_e(8)
  @inline @cCode.define @cCode.export val E_MEMFILESYSTEM_FILETYPE_LAST = MemFilesystem_filetype_e(9)
}
