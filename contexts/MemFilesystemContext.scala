/* Copyright 2021 Ateleris GmbH, Brugg */

package contexts

import definitions.FilesystemDefinitions._
import definitions.PartitionDefinitions._
import stainless.annotation._
import stainless.lang.StaticChecks._

object MemFilesystemContainer {

}
@inlineInvariant @cCode.`export`
case class MemFilesystemContainer(
                                  val controlBlocks: Array[FileControlBlock]
                            ) {
  require(
    controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt
  )

  @inline @ghost
  def ensureInvariant(): Unit = {
  } ensuring(_ =>
    controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt
  )

  @inline @ghost
  def ensureLength(): Unit = {
  }.ensuring(_ => controlBlocks.length == TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt)
}

@cCode.globalUninitialized
case class MemFilesystemContext(
                                 var memFilesystem: MemFilesystemContainer,
                               ) {
}
