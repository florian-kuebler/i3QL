package sandbox.stackAnalysis

import datastructure._
import sandbox.dataflowAnalysis.DataFlowAnalysis
import sae.bytecode.structure.CodeInfo
import de.tud.cs.st.bat.resolved.ObjectType


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
object StackAnalysis extends DataFlowAnalysis[State](CodeInfoCFG, CodeInfoTransformer) {

  override def startValue(ci: CodeInfo): State = {
    var stacks = Stacks(ci.code.maxStack, Nil).addStack()

    var lvs = if (ci.declaringMethod.isStatic)
      LocVariables(Array.fill[Item](ci.code.maxLocals)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED)))
    else
      LocVariables(Array.fill[Item](ci.code.maxLocals)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED))).setVar(0, Item(ItemType.SomeRef(ObjectType.Class), -1, Item.FLAG_IS_PARAMETER))

    var i: Int = if (ci.declaringMethod.isStatic) -1 else 0

    for (t <- ci.declaringMethod.parameterTypes) {
      i = i + 1
      lvs = lvs.setVar(i, Item(ItemType.fromType(t), -1, Item.FLAG_IS_PARAMETER))
    }

    State(stacks, lvs)
  }

  override def emptyValue(ci: CodeInfo): State = {
    State(Stacks(ci.code.maxStack, Nil), LocVariables(Array.ofDim[Item](ci.code.maxLocals)))
  }


}
