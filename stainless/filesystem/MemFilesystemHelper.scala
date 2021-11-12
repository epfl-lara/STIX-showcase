package filesystem

import stainless.annotation._
import stainless.lang.{BooleanDecorations, decreases, ghost, old}
import stainless.lang.StaticChecks._
import stainless.math.BitVectors._
import definitions.FilesystemDefinitions._
import filesystem.SortedArray.{isSorted, isSortedRange}


object MemFilesystemHelper {
  object QueueElement {
    @pure @inline @ghost
    def invariant(cbIdx: Int, fileIndex: UInt32) = {
      cbIdx >= 0 &&&
      cbIdx < TOTAL_NUMBER_OF_CONTROL_BLOCKS.toInt &&&
      fileIndex < FILES_PER_FILECB
    }
  }
  case class QueueElement(
    cbIdx: Int,
    fileIndex: UInt32,
    coarse: UInt32,
    fine: UInt16,
    duration: UInt16,
  ) {
    require(
      QueueElement.invariant(cbIdx, fileIndex)
    )

    @inlineOnce @opaque @ghost
    def ensureInvariant(): Unit = {

    }.ensuring(_ =>
      QueueElement.invariant(cbIdx, fileIndex)
    )
  }

  @inlineInvariant
  case class PriorityQueue(sortedArray: SortedArray[TimingSCET, QueueElement], min: TimingSCET, max: TimingSCET) {
    require(
      sortedArray.order == TimingComparison.TimingSCETOrder &&
      SortedArray.isSorted(sortedArray.array, sortedArray.order) &&
      sortedArray.length > 0
    )
    @inline def length: Int = sortedArray.length

    @inline def apply(i: Int): (TimingSCET, QueueElement) = {
      require(0 <= i && i < sortedArray.length)
      sortedArray(i)
    }

    @inline def insert(elemRef: Array[(TimingSCET, QueueElement)]) = {
      require(elemRef.length == 1)
      sortedArray.insert(elemRef)
    }.ensuring(_ => sortedArray.array.length == old(this).sortedArray.array.length)
  }

  @opaque @inlineOnce @ghost
  def fillZeroIsSortedRange(to: TimingSCET, i: Int, l: Int): Unit = {
    require(i >= 0)
    require(l >= 1)

    if (0 <= i && i < l - 1 && i < l) {
      fillZeroIsSortedRange(to, i+1, l)
    }

  }.ensuring(_ => isSortedRange(Array.fill[(TimingSCET, QueueElement)](l)(
    to, QueueElement(0, 0, to.coarse, to.fine, 0)
  ), TimingComparison.TimingSCETOrder, i, l-1))

  @opaque @inline @ghost
  def fillZeroIsSorted(to: TimingSCET, l: Int): Unit = {
    require(l >= 0)
    if (l > 0)
      fillZeroIsSortedRange(to, 0, l)
  }.ensuring(_ => isSorted(Array.fill[(TimingSCET, QueueElement)](l)(
    to, QueueElement(0, 0, to.coarse, to.fine, 0)
  ), TimingComparison.TimingSCETOrder))

  @cCode.export @inline @cCode.inline
  def initPrioQueue(l: Int, to: TimingSCET, from: TimingSCET): Unit = {
    require(l > 0)
    require(l < SortedArray.sizeLimit)

    ghost {
      fillZeroIsSorted(to, l)
    }
    PriorityQueue(
      SortedArray[TimingSCET, QueueElement](
        Array.fill[(TimingSCET, QueueElement)](l)(
          to, QueueElement(0, 0, to.coarse, to.fine, 0)
        ),
        TimingComparison.TimingSCETOrder
      ),
      from, to
    )
    ()
  }

  @opaque @inlineOnce
  def insertIntoPrioQueue(queue: PriorityQueue, time: TimingSCET, elem: QueueElement): Boolean = {
    require(queue.length > 0)
    require(time.coarse <= max[UInt32] / 2)

    val c = time.coarse
    val f = time.fine
    val d: UInt32 = elem.duration.widen[UInt32]

    val minC = queue.min.coarse
    val minF = queue.min.fine
    val maxC = queue.max.coarse
    val maxF = queue.max.fine

    // within range with file beginning
    val caseA =
      TimingComparison.leq(minC, minF, c, f) &&
      TimingComparison.leq(c, f, maxC, maxF)

    // file starts below range, but file ends in range
    val caseB =
      TimingComparison.leq(minC, minF, c + d, f) &&
      TimingComparison.leq(c + d, f, maxC, maxF)

    // file starts below range and ends after range
    val caseC =
      TimingComparison.leq(c, f, minC, minF) &&
      TimingComparison.leq(maxC, maxF, c + d, f)

    // valid range is marked below, invert it (less logic)
    if (caseA || caseB || caseC) {
      queue.insert(Array((time, elem)))
    } else {
      false
    }
  }.ensuring(_ => queue.length == old(queue).length)

  def getHighestPrioElement(queue: PriorityQueue): QueueElement = {
    require(queue.length > 0)

    queue(0)._2
  }

  def getLeastPrioElement(queue: PriorityQueue): QueueElement = {
    require(queue.length > 0)

    queue(queue.length - 1)._2
  }
}
