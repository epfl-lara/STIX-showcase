package filesystem.contexts

import stainless.annotation._
import stainless.math.BitVectors._

@cCode.globalExternal
case class FlashContext(
                        var badBlockCount: UInt32,
                        var errorBlockCount: UInt32,
                        var usedBlockCount: UInt32,
                      ) {
}
