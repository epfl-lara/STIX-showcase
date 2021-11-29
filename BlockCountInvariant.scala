/* Copyright 2021 Ateleris GmbH, Brugg */

import definitions.FilesystemDefinitions._
import definitions.PartitionDefinitions._
import contexts.FlashContext
import stainless.annotation._
import stainless.lang._
import stainless.math.BitVectors.{max => _, _}
import stainless.proof.check

object BlockCountInvariant {

  @ghost @pure
  def countStatusFrom(controlBlocks: Array[FileControlBlock], status: File_blockStatus_e, cbIdx: Int, blockNumber: Int): UInt32 = {
    require(0 <= cbIdx && cbIdx <= TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber <= FILES_PER_FILECB.toInt)
    decreases(
      TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt - cbIdx,
      FILES_PER_FILECB.toInt - blockNumber,
    )

    if (cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt) {
      controlBlocks(cbIdx).ensureInvariant()
      if (blockNumber < FILES_PER_FILECB.toInt) {
        if (controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus == status)
          countStatusFrom(controlBlocks, status, cbIdx, blockNumber + 1) + 1
        else
          countStatusFrom(controlBlocks, status, cbIdx, blockNumber + 1)
      }
      else
        countStatusFrom(controlBlocks, status, cbIdx + 1, 0)
    }
    else
      (0 : UInt32)
  }.ensuring(res =>
    res <= 65400 &&&
    (cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt - 1 ==>
      res.toInt <= (TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt - 1 - cbIdx) * 60 + FILES_PER_FILECB.toInt - blockNumber) &&&
    (cbIdx == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt - 1 ==>
      res.toInt <= FILES_PER_FILECB.toInt - blockNumber) &&&
    (cbIdx == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt ==>
      (res == 0))
  )

  @ghost @pure @opaque
  def countStatus(controlBlocks: Array[FileControlBlock], status: File_blockStatus_e): UInt32 = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    countStatusFrom(controlBlocks, status, 0, 0)
  }.ensuring(res =>
    res <= 65400
  )

  @ghost @pure @opaque
  def blockCountInvariant(controlBlocks: Array[FileControlBlock], flashContext: FlashContext): Boolean = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)

    countStatus(controlBlocks, E_FILE_BLOCK_STAT_FILE) == flashContext.usedBlockCount &&&
    countStatus(controlBlocks, E_FILE_BLOCK_STAT_ERROR) == flashContext.errorBlockCount &&&
    countStatus(controlBlocks, E_FILE_BLOCK_STAT_BAD) == flashContext.badBlockCount
  }

  @ghost @pure @opaque @inlineOnce
  def atLeastOneBlockFrom(
    controlBlocks: Array[FileControlBlock],
    status: File_blockStatus_e,
    cbIdx0: Int,
    blockNumber0: Int,
    cbIdx: Int,
    blockNumber: Int,
  ): Unit = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= cbIdx0 && cbIdx0 < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber0 && blockNumber0 < FILES_PER_FILECB.toInt)
    require(cbIdx >= 0)
    require(0 <= blockNumber && blockNumber <= FILES_PER_FILECB.toInt)
    require(cbIdx < cbIdx0 || (cbIdx == cbIdx0 && blockNumber <= blockNumber0))
    require { controlBlocks(cbIdx0).ensureInvariant(); true }
    require(controlBlocks(cbIdx0).fileCatalog(blockNumber0).blockStatus == status)

    if (cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt) {
      controlBlocks(cbIdx).ensureInvariant()
      if (blockNumber < FILES_PER_FILECB.toInt) {
        if (controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus == status) {
          assert(
            countStatusFrom(controlBlocks, status, cbIdx, blockNumber) ==
            1 + countStatusFrom(controlBlocks, status, cbIdx, blockNumber + 1)
          )
          assert(countStatusFrom(controlBlocks, status, cbIdx, blockNumber + 1) >= 0)
          assert(countStatusFrom(controlBlocks, status, cbIdx, blockNumber + 1) <= 65400)
          check(countStatusFrom(controlBlocks, status, cbIdx, blockNumber) > 0)
        }
        else if (cbIdx < cbIdx0 || (cbIdx == cbIdx0 && blockNumber < blockNumber0))
          atLeastOneBlockFrom(controlBlocks, status, cbIdx0, blockNumber0, cbIdx, blockNumber + 1)
        else {
          ()
        }
      }
      else if (cbIdx < cbIdx0)
        atLeastOneBlockFrom(controlBlocks, status, cbIdx0, blockNumber0, cbIdx + 1, 0)
      else
        ()
    }

  }.ensuring(_ => countStatusFrom(controlBlocks, status, cbIdx, blockNumber) > 0)

  @ghost @pure @opaque @inlineOnce
  def atLeastOneBlock(
    controlBlocks: Array[FileControlBlock],
    status: File_blockStatus_e,
    cbIdx: Int,
    blockNumber: Int
  ): Unit = {
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require { controlBlocks(cbIdx).ensureFileCatalogLength(); true }
    require(controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus == status)

    unfold(countStatus(controlBlocks, status))
    atLeastOneBlockFrom(controlBlocks, status, cbIdx, blockNumber, 0, 0)

  }.ensuring(_ => countStatus(controlBlocks, status) > 0)

  @ghost @pure @opaque @inlineOnce
  def smallCount(controlBlocks: Array[FileControlBlock], flashContext: FlashContext): Unit = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(blockCountInvariant(controlBlocks, flashContext))

    unfold(blockCountInvariant(controlBlocks, flashContext))

  }.ensuring(_ =>
    flashContext.usedBlockCount <= MAX_NUMBER_OF_FILES &&&
    flashContext.errorBlockCount <= MAX_NUMBER_OF_FILES &&&
    flashContext.badBlockCount <= MAX_NUMBER_OF_FILES
  )

  @ghost @pure @inline
  def withStatus(cb: FileControlBlock, blockNumber: Int, newStatus: File_blockStatus_e): FileControlBlock = {
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require { cb.ensureFileCatalogLength(); true }
    require { cb.ensureInvariant(); true }

    val newNode = FileCatalogNode(newStatus)
    val newCatalog = snapshot(cb.fileCatalog.updated(blockNumber, newNode))
    snapshot(FileControlBlock(
      cb.controlBlockNumber,
      newCatalog,
      cb.freeBlocksBitMap,
    ))
  }

  @ghost @pure @inline
  def withStatus(controlBlocks: Array[FileControlBlock], cbIdx: Int, blockNumber: Int, newStatus: File_blockStatus_e): Array[FileControlBlock] = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require { controlBlocks(cbIdx).ensureFileCatalogLength(); true }

    val newBlock = withStatus(controlBlocks(cbIdx), blockNumber, newStatus)
    snapshot(controlBlocks.updated(cbIdx, newBlock))
  }

  @ghost @pure @opaque @inlineOnce
  def changeStatusFrom(
    controlBlocks: Array[FileControlBlock],
    status: File_blockStatus_e,
    cbIdx: Int,
    blockNumber: Int,
    newStatus: File_blockStatus_e,
    i: Int,
    j: Int,
  ): Unit = {
    require(0 <= i && i <= TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= j && j <= FILES_PER_FILECB.toInt)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require { controlBlocks(cbIdx).ensureFileCatalogLength(); true }
    require(i >= cbIdx && (i == cbIdx ==> j > blockNumber))
    val oldStatus = controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus
    val controlBlocksCopy = withStatus(controlBlocks, cbIdx, blockNumber, newStatus)

    {

      if (i < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt) {
        if (j < FILES_PER_FILECB.toInt) {
          changeStatusFrom(controlBlocks, status, cbIdx, blockNumber, newStatus, i, j+1)
        } else {
          changeStatusFrom(controlBlocks, status, cbIdx, blockNumber, newStatus, i+1, 0)
        }
      }

    }.ensuring { _ =>
      countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j)
    }
  }

  @ghost @pure @opaque @inlineOnce
  def changeStatusFrom2(
    controlBlocks: Array[FileControlBlock],
    status: File_blockStatus_e,
    cbIdx: Int,
    blockNumber: Int,
    newStatus: File_blockStatus_e,
    i: Int,
    j: Int,
  ): Unit = {
    require(0 <= i && i <= TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= j && j <= FILES_PER_FILECB.toInt)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require { controlBlocks(cbIdx).ensureFileCatalogLength(); true }
    require(i < cbIdx || (i == cbIdx && j <= blockNumber))
    val oldStatus = controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus
    val controlBlocksCopy = withStatus(controlBlocks, cbIdx, blockNumber, newStatus)

    {

      if (i < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt) {
        if (j < FILES_PER_FILECB.toInt)
          if (i == cbIdx && j == blockNumber)
            changeStatusFrom(controlBlocks, status, cbIdx, blockNumber, newStatus, i, j+1)
          else
            changeStatusFrom2(controlBlocks, status, cbIdx, blockNumber, newStatus, i, j+1)
        else
          changeStatusFrom2(controlBlocks, status, cbIdx, blockNumber, newStatus, i+1, 0)
      }

    }.ensuring { _ =>
      ((newStatus == oldStatus && status == newStatus) ==> (countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j))) &&&
      ((newStatus != oldStatus && status == newStatus) ==> (countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j) + 1)) &&&
      ((newStatus != oldStatus && status == oldStatus) ==> (countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j) - 1)) &&&
      ((newStatus != status && oldStatus != status) ==> (countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j)))
    }
  }

  @ghost @pure @opaque @inlineOnce
  def changeStatus(
    controlBlocks: Array[FileControlBlock],
    status: File_blockStatus_e,
    cbIdx: Int,
    blockNumber: Int,
    newStatus: File_blockStatus_e,
  ): Unit = {
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= blockNumber && blockNumber < FILES_PER_FILECB.toInt)
    require { controlBlocks(cbIdx).ensureFileCatalogLength(); true }
    val oldStatus = controlBlocks(cbIdx).fileCatalog(blockNumber).blockStatus
    val controlBlocksCopy = withStatus(controlBlocks, cbIdx, blockNumber, newStatus)

    {
      changeStatusFrom2(controlBlocks, status, cbIdx, blockNumber, newStatus, 0, 0)
      unfold(countStatus(controlBlocksCopy, status))
      unfold(countStatus(controlBlocks, status))

    }.ensuring { _ =>
      ((newStatus == oldStatus && status == newStatus) ==> (countStatus(controlBlocksCopy, status) == countStatus(controlBlocks, status))) &&&
      ((newStatus != oldStatus && status == newStatus) ==> (countStatus(controlBlocksCopy, status) == countStatus(controlBlocks, status) + 1)) &&&
      ((newStatus != oldStatus && status == oldStatus) ==> (countStatus(controlBlocksCopy, status) == countStatus(controlBlocks, status) - 1)) &&&
      ((newStatus != status && oldStatus != status) ==> (countStatus(controlBlocksCopy, status) == countStatus(controlBlocks, status)))
    }
  }

  @ghost @pure @opaque @inlineOnce
  def unchangedCountFrom(controlBlocks: Array[FileControlBlock], cbIdx: Int, cb: FileControlBlock, status: File_blockStatus_e, i: Int, j: Int): Unit = {
    require(0 <= i && i <= TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(0 <= j && j <= FILES_PER_FILECB.toInt)
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks(cbIdx).fileCatalog == cb.fileCatalog)
    val controlBlocksCopy = controlBlocks.updated(cbIdx, cb)

    {
      if (i < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt) {
        if (j < FILES_PER_FILECB.toInt)
          unchangedCountFrom(controlBlocks, cbIdx, cb, status, i, j+1)
        else
          unchangedCountFrom(controlBlocks, cbIdx, cb, status, i+1, 0)
      }
    }.ensuring { _ =>
      countStatusFrom(controlBlocksCopy, status, i, j) == countStatusFrom(controlBlocks, status, i, j)
    }
  }

  @ghost @pure @opaque @inlineOnce
  def unchangedCount(controlBlocks: Array[FileControlBlock], cbIdx: Int, cb: FileControlBlock, status: File_blockStatus_e): Unit = {
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks(cbIdx).fileCatalog == cb.fileCatalog)
    val controlBlocksCopy = controlBlocks.updated(cbIdx, cb)

    {
      unchangedCountFrom(controlBlocks, cbIdx, cb, status, 0, 0)
      unfold(countStatus(controlBlocks, status))
      unfold(countStatus(controlBlocksCopy, status))
    }.ensuring { _ =>
      countStatus(controlBlocksCopy, status) == countStatus(controlBlocks, status)
    }
  }

  @ghost @pure @opaque @inlineOnce
  def blockCountInvariantUnchanged(controlBlocks: Array[FileControlBlock], flashContext: FlashContext, cbIdx: Int, cb: FileControlBlock): Unit = {
    require(0 <= cbIdx && cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
    require(controlBlocks(cbIdx).fileCatalog == cb.fileCatalog)
    require(blockCountInvariant(controlBlocks, flashContext))

    val controlBlocksCopy = controlBlocks.updated(cbIdx, cb)

    {
      unfold(blockCountInvariant(controlBlocks, flashContext))
      unfold(blockCountInvariant(controlBlocksCopy, flashContext))
      unchangedCount(controlBlocks, cbIdx, cb, E_FILE_BLOCK_STAT_BAD)
      unchangedCount(controlBlocks, cbIdx, cb, E_FILE_BLOCK_STAT_FILE)
      unchangedCount(controlBlocks, cbIdx, cb, E_FILE_BLOCK_STAT_ERROR)

    }.ensuring { _ =>
      blockCountInvariant(controlBlocksCopy, flashContext)
    }
  }

}
