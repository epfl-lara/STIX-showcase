package filesystem.definitions

import FilesystemDefinitions._
import stainless.lang.BooleanDecorations
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

  @cCode.export
  case class FileCatalogNode(var blockStatus: File_blockStatus_e) { }

  object FileControlBlock {

    @inline @ghost @cCode.inline
    def invariant(
                   fileCatalog: Array[FileCatalogNode],
                   freeBlocksBitMap: Array[UInt32],
                 ): Boolean = {
        fileCatalog.length == FILES_PER_FILECB.toInt &&&
        freeBlocksBitMap.length == (FILES_PER_FILECB.toInt + 31) / 32
    }

  }

  @inlineInvariant @cCode.export
  case class FileControlBlock(
                               var controlBlockNumber: UInt32, // cb index in memFilesystemContainer

                               var fileCatalog: Array[FileCatalogNode],
                               var freeBlocksBitMap: Array[UInt32],
                             ) {
    require(FileControlBlock.invariant(fileCatalog, freeBlocksBitMap))

    @ghost @inline @pure
    def ensureInvariant(): Unit = {
    } ensuring (_ => FileControlBlock.invariant(fileCatalog, freeBlocksBitMap))

    @ghost @inline @pure
    def ensureBitMapLength(): Unit = {
    }.ensuring(_ => freeBlocksBitMap.length == (FILES_PER_FILECB.toInt + 31) / 32)

    @ghost @inline @pure
    def ensureFileCatalogLength(): Unit = {
    }.ensuring(_ => fileCatalog.length == FILES_PER_FILECB.toInt)
  }
}
