package filesystem.contexts

import filesystem.definitions.FilesystemDefinitions._
import filesystem.definitions.PartitionDefinitions._
import stainless.annotation._
import stainless.lang.StaticChecks._

object MemFilesystemContainer {

}
@inlineInvariant @cCode.export
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