/* Copyright 2021 Ateleris GmbH, Brugg */

import stainless.math.BitVectors._
import stainless.annotation._

// Spacecraft Elapsed Time
@inlineInvariant
case class TimingSCET(
  coarse: UInt32,
  fine: UInt16,
)

object TimingComparison {
  def leq(c1: UInt32, f1: UInt16, c2: UInt32, f2: UInt16): Boolean = {
    c1 < c2 || (c1 == c2 && f1 <= f2)
  }

  def leq(t1: TimingSCET, t2: TimingSCET): Boolean = {
    t1.coarse < t2.coarse || (t1.coarse == t2.coarse && t1.fine <= t2.fine)
  }

  val TimingSCETOrder = TotalOrder[TimingSCET](leq)
}
