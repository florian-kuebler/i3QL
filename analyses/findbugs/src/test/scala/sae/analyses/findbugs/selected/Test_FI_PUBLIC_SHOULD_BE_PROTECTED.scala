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
package sae.analyses.findbugs.selected

import org.junit.Test
import org.junit.Assert._
import sae.bytecode.asm.ASMDatabaseFactory
import sae.bytecode.{BytecodeDatabase, BytecodeAccessFlags}


/**
 *
 * @author Ralf Mitschke
 *
 */
class Test_FI_PUBLIC_SHOULD_BE_PROTECTED
{

    @Test
    def truePositive () {
        val database : BytecodeDatabase = null
        //import database._
        val analysis = FI_PUBLIC_SHOULD_BE_PROTECTED (database).asMaterialized

        val clazz = ClassDeclaration (0, 0, ObjectType ("test/Test"), Some (Object), Nil)

        val method = MethodDeclaration (clazz, BytecodeAccessFlags.ACC_PUBLIC, "finalize", void, Nil)

        database.methodDeclarations += method
        assertEquals (
            List (
                method
            ),
            analysis.asList
        )
    }


    @Test
    def trueNegative () {
        val database : BytecodeDatabase = null
        //val database = ASMDatabaseFactory.create ()
        //import database._
        val analysis = FI_PUBLIC_SHOULD_BE_PROTECTED (database).asMaterialized

        val clazz = ClassDeclaration (0, 0, ObjectType ("test/Test"), Some (Object), Nil)

        val method = MethodDeclaration (clazz, BytecodeAccessFlags.ACC_PROTECTED, "finalize", void, Nil)

        database.methodDeclarations += method
        assertEquals (
            Nil,
            analysis.asList
        )
    }

}