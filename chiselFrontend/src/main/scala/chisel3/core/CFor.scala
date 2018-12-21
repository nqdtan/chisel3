// See LICENSE for license details.

package chisel3.core

import scala.language.experimental.macros

import chisel3.internal._
import chisel3.internal.Builder.pushCommand
import chisel3.internal.firrtl._
import chisel3.internal.sourceinfo.{SourceInfo}

object cFor {  // scalastyle:ignore object.name
  def apply[T <: Data](index:  T,
                       min:    T,
                       extent: T,
                       stride: T)(block: => Unit)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): CForContext[T] = {
    new CForContext(sourceInfo, index, min, extent, stride, block)
  }
}

final class CForContext[T <: Data](sourceInfo: SourceInfo,
                                   index:  T,
                                   min:    T,
                                   extent: T,
                                   stride: T,
                                   block: => Unit,
                                   firrtlDepth: Int = 0) {
  pushCommand(CForBegin(sourceInfo, index.ref, min.ref, extent.ref, stride.ref))
  Builder.cForDepth += 1
  try {
    block
  } catch {
    case ret: scala.runtime.NonLocalReturnControl[_] =>
      throwException("Cannot exit from a cFor() block with a \"return\"!" +
        " Perhaps you meant to use Mux or a Wire as a return value?"
      )
  }
  Builder.cForDepth -= 1
  pushCommand(CForEnd(sourceInfo,firrtlDepth))
}
