package sandbox.findbugs.detect

import de.tud.cs.st.bat.resolved.{Instruction, INVOKEVIRTUAL, ObjectType}
import sandbox.stackAnalysis.datastructure.{LocalVariables, Stack, State}
import sandbox.findbugs.BugType
import sae.bytecode.structure.CodeInfo

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 17.12.12
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
object MWN_MISMATCHED_NOTIFY extends Detector {

  def getDetectorFunction(instr: Instruction): (Int, Instruction, Stack, LocalVariables) => Option[BugType.Value] = {

    if (instr.isInstanceOf[INVOKEVIRTUAL]) {
      val invInstr = instr.asInstanceOf[INVOKEVIRTUAL]

      if (invInstr.declaringClass.equals(ObjectType.Object) &&
        (invInstr.name.equals("notify") || invInstr.name.equals("notifyAll"))) {
        return checkMismatchedNotify
      }
    }

    return checkNone


  }

  private def checkMismatchedNotify(pc: Int, instr: Instruction, stack: Stack, loc: LocalVariables): Option[BugType.Value] = {
    None
  }
}
