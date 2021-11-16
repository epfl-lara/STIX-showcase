/* Copyright 2021 Ateleris GmbH, Brugg */

package filesystem

import filesystem.contexts._
import filesystem.definitions.FilesystemDefinitions._
import filesystem.definitions.PartitionDefinitions._
import filesystem.BlockCountInvariant._
import stainless.annotation._
import stainless.lang.StaticChecks.{assert => assert2, _}
import stainless.lang.{BooleanDecorations, ghost, old, snapshot, unfold}
import stainless.math.BitVectors._
import stainless.proof._

object File {

  @cCode.inline @inlineOnce @opaque
  def freeBlocksBitMapOr(cbIdx: Int, k: Int, v: UInt32)(implicit memFSContext: MemFilesystemContext, flashContext: FlashContext): Unit = {
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= k && k < (FILES_PER_FILECB.toInt + 31) / 32)
    require { memFSContext.memFilesystem.ensureLength(); true }
    require(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))

    ghost {
      val cb0 = snapshot(memFSContext.memFilesystem.controlBlocks(cbIdx))
      cb0.ensureInvariant()
      cb0.freeBlocksBitMap(k) |= v
      // blockCountInvariantFreeBlocksBitMap(memFSContext.memFilesystem.controlBlocks, flashContext, cbIdx, k, cb0.freeBlocksBitMap(k) | v)
      memFSContext.memFilesystem.ensureLength()
      memFSContext.memFilesystem.controlBlocks(cbIdx).ensureBitMapLength()
      blockCountInvariantUnchanged(memFSContext.memFilesystem.controlBlocks, flashContext, cbIdx, cb0)
    }

    memFSContext.memFilesystem.controlBlocks(cbIdx).freeBlocksBitMap(k) |= v

  }.ensuring(_ =>
    {
      memFSContext.memFilesystem.ensureLength()
      blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext)
    }
  )

  @cCode.export @inlineOnce @opaque
  private def setBlockAsFree(cbIdx: Int, blockNumber: UInt32)(implicit memFSContext: MemFilesystemContext, flashContext: FlashContext) = {
    require(blockNumber < FILES_PER_FILECB)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require { memFSContext.memFilesystem.ensureLength(); true }
    require(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))

    ghost {
      memFSContext.memFilesystem.controlBlocks(cbIdx).ensureFileCatalogLength()
    }

    val k = blockNumber.toInt / BITS_IN_INT32.toInt
    val v = 1 << (blockNumber % BITS_IN_INT32)
    freeBlocksBitMapOr(cbIdx, k, v)

    ghost {
      memFSContext.memFilesystem.ensureLength()
      memFSContext.memFilesystem.controlBlocks(cbIdx).ensureFileCatalogLength()
    }

    val oldStatus = memFSContext.memFilesystem.controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus

    ghost {
      atLeastOneBlock(memFSContext.memFilesystem.controlBlocks, oldStatus, cbIdx, blockNumber.toInt)
      smallCount(memFSContext.memFilesystem.controlBlocks, flashContext)
      memFSContext.memFilesystem.ensureLength()
      unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
    }

    updateStatsBlockTransition(oldStatus, E_FILE_BLOCK_STAT_FREE)

    @ghost val memFSContext0 = snapshot(memFSContext)

    {
      assert2 { // we make an `assert` so that all this code disappears for the solver after the `blockStatus` update
        memFSContext.memFilesystem.ensureInvariant()
        val cb1 = snapshot(memFSContext.memFilesystem.controlBlocks(cbIdx))
        cb1.ensureInvariant()
        cb1.fileCatalog(blockNumber).blockStatus = E_FILE_BLOCK_STAT_FREE
        memFSContext.memFilesystem.controlBlocks(cbIdx).ensureFileCatalogLength()
        true
      }
      memFSContext.memFilesystem.controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus = E_FILE_BLOCK_STAT_FREE
    }

    ghost {
      memFSContext0.memFilesystem.ensureLength()
      unfold(blockCountInvariant(memFSContext0.memFilesystem.controlBlocks, flashContext))
      changeStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE, cbIdx, blockNumber.toInt, E_FILE_BLOCK_STAT_FREE)
      changeStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR, cbIdx, blockNumber.toInt, E_FILE_BLOCK_STAT_FREE)
      changeStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD, cbIdx, blockNumber.toInt, E_FILE_BLOCK_STAT_FREE)

      if (oldStatus == E_FILE_BLOCK_STAT_FILE) {
        memFSContext.memFilesystem.ensureLength()
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE) - 1)
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD))
        unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
        check(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
      } else if (oldStatus == E_FILE_BLOCK_STAT_ERROR) {
        memFSContext.memFilesystem.ensureLength()
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR) - 1)
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD))
        unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
        check(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
      } else if (oldStatus == E_FILE_BLOCK_STAT_BAD) {
        memFSContext.memFilesystem.ensureLength()
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD) - 1)
        unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
        check(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
      } else {
        memFSContext.memFilesystem.ensureLength()
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_FILE))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_ERROR))
        assert(countStatus(memFSContext.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD) == countStatus(memFSContext0.memFilesystem.controlBlocks, E_FILE_BLOCK_STAT_BAD))
        unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
        check(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
      }
      memFSContext.memFilesystem.ensureLength()
      unfold(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
      check(blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext))
    }

  }.ensuring(_ =>
    {
      memFSContext.memFilesystem.ensureLength()
      blockCountInvariant(memFSContext.memFilesystem.controlBlocks, flashContext)
    } &&& {
      memFSContext.memFilesystem.ensureLength()
      old(memFSContext).memFilesystem.ensureLength()
      true
    }
  )

  @inline
  private def updateStatsBlockTransition(
                                          from: File_blockStatus_e,
                                          to: File_blockStatus_e
                                        )(implicit
                                          memFSContext: MemFilesystemContext,
                                          flashContext: FlashContext
  ) = {
    import flashContext._
    require((from == E_FILE_BLOCK_STAT_FILE)  ==> usedBlockCount > 0)
    require((from == E_FILE_BLOCK_STAT_ERROR) ==>  errorBlockCount > 0)
    require(from == E_FILE_BLOCK_STAT_BAD ==> badBlockCount > 0)
    require(usedBlockCount <= MAX_NUMBER_OF_FILES)
    require(errorBlockCount <= MAX_NUMBER_OF_FILES)
    require(badBlockCount <= MAX_NUMBER_OF_FILES)

    if (from == E_FILE_BLOCK_STAT_FILE)
      usedBlockCount = usedBlockCount - 1
    else if (from == E_FILE_BLOCK_STAT_ERROR)
      errorBlockCount = errorBlockCount - 1
    else if (from == E_FILE_BLOCK_STAT_BAD)
      badBlockCount = badBlockCount - 1


    if (to == E_FILE_BLOCK_STAT_FILE)
      usedBlockCount = usedBlockCount + 1
    else if (to == E_FILE_BLOCK_STAT_ERROR)
      errorBlockCount = errorBlockCount + 1
    else if (to == E_FILE_BLOCK_STAT_BAD)
      badBlockCount = badBlockCount + 1
  }

}
