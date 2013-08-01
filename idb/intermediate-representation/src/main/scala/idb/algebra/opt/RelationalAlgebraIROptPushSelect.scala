/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.algebra.opt

import scala.virtualization.lms.common._
import idb.algebra.ir.RelationalAlgebraIRBasicOperators
import idb.lms.extensions.{FunctionUtils, ExpressionUtils}

/**
 *
 * @author Ralf Mitschke
 *
 */
trait RelationalAlgebraIROptPushSelect
    extends RelationalAlgebraIRBasicOperators
    with LiftBoolean
    with BooleanOps
    with BooleanOpsExp
    with EffectExp
    with FunctionsExp
    with ExpressionUtils
    with FunctionUtils
{

    /**
     * Pushing selection down over other operations
     */
    override def selection[Domain: Manifest] (
        relation: Rep[Query[Domain]],
        function: Rep[Domain => Boolean]
    ): Rep[Query[Domain]] = {
        relation match {
            // pushing over projections
            case Def (Projection (r, f)) => {
                val pushedFunction = fun ((x: Exp[Any]) => function (f (x)))(
                    f.tp.typeArguments (0).asInstanceOf[Manifest[Any]], manifest[Boolean])
                projection (selection (r, pushedFunction), f)
            }
            // pushing selections that only use their arguments partially over selections that need all arguments
            case Def (Selection (r, f)) if freeVars (f).isEmpty && !freeVars (function).isEmpty =>
                super.selection (super.selection (relation, f), function)

            case _ =>
                super.selection (relation, function)
        }
    }


}
