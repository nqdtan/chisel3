// See LICENSE for license details.

package chisel3.core

import scala.language.experimental.macros

import chisel3.internal._
import chisel3.internal.Builder.pushCommand
import chisel3.internal.firrtl._
import chisel3.internal.sourceinfo.{SourceInfo}

object cFor {  // scalastyle:ignore object.name
  def apply(cond: => Bool)(block: => Unit)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): CForContext = {
    new CForContext(sourceInfo, Some(() => cond), block)
  }
}

final class CForContext(sourceInfo: SourceInfo, cond: Option[() => Bool], block: => Unit, firrtlDepth: Int = 0) {
  cond.foreach( c => pushCommand(CForBegin(sourceInfo, c().ref)) )
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
  cond.foreach( c => pushCommand(CForEnd(sourceInfo,firrtlDepth)) )
}
