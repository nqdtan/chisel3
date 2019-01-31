// See LICENSE for license details.

package chisel3.core

import scala.language.experimental.macros

import chisel3.internal._
import chisel3.internal.Builder.pushCommand
import chisel3.internal.firrtl._
import chisel3.internal.sourceinfo.{SourceInfo}

object loop {  // scalastyle:ignore object.name
  def apply(cond: => Bool)(block: => Unit)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): LoopContext = {
    new LoopContext(sourceInfo, Some(() => cond), block)
  }
}

final class LoopContext(sourceInfo: SourceInfo,
                        cond: Option[() => Bool],
                        block: => Unit,
                        firrtlDepth: Int = 0) {
  cond.foreach(c => pushCommand(LoopBegin(sourceInfo, c().ref)))
  Builder.loopDepth += 1
  try {
    block
  } catch {
    case ret: scala.runtime.NonLocalReturnControl[_] =>
      throwException("Cannot exit from a loop() block with a \"return\"!" +
        " Perhaps you meant to use Mux or a Wire as a return value?"
      )
  }
  Builder.loopDepth -= 1
  cond.foreach(c => pushCommand(LoopEnd(sourceInfo,firrtlDepth)))
}
