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
package idb.lms.extensions

import org.junit.{Ignore, Test}
import org.junit.Assert._
import scala.virtualization.lms.common._

/**
 *
 * @author Ralf Mitschke
 */
class TestFunctionConversion
    extends FunctionCreator
{

    val IR = new BaseFatExp
        with NumericOpsExp
        with EffectExp
        with EqualExp
        with TupledFunctionsExp
        with TupleOpsExp
        with FunctionsExpOptAlphaEquivalence
        with ExpressionUtils
        with LiftAll

    import IR.Rep
    import IR.fresh
    import IR.unbox
    import IR.fun
    import IR.repNumericToNumericOps
    import IR.numericToNumericOps
    import IR.doApply
    import IR.UnboxedTuple
    import IR.make_tuple2

    @Test
    def testFun1Recreate () {
        val f = (i: Rep[Int]) => {1 + i }
        val x = fresh[Int]
        val body = f (x)

        val g = recreateFun (x, body)

        assertEquals (fun (f), fun (g))
    }

    @Test
    def testFun2Recreate () {
        val f = (i: Rep[Int], j: Rep[Int]) => {i + j }
        val funF = fun (f)

        val x = fresh[Int]
        val y = fresh[Int]
        val body = f (x, y)

        val params: Rep[(Int,Int)] =  (x,y)

        val g = recreateFun (params, body)

        val funG = fun (g)
        assertEquals (funF, funG)
    }


    @Test
    def testFun2AsUnboxedTupleRecreate () {
        val f = (i: Rep[Int], j: Rep[Int]) => {i + j }
        val funF = fun (f)

        val x = fresh[Int]
        val y = fresh[Int]
        val body = f (x, y)

        val params: Rep[(Int,Int)] =  UnboxedTuple(List(x,y))

        val g = recreateFun (params, body)

        val funG = fun (g)
        assertEquals (funF, funG)
    }
}
