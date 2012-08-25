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
package sae.bytecode.profiler

import observers.ElementCounter
import sae.bytecode._
import java.io.FileInputStream


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 15:52
 *
 * For guid on instrumentation read: http://www.developerfusion.com/article/84353/new-features-in-java-15/
 *
 */

import java.lang.instrument.Instrumentation

object MemoryProfiler
    extends MemoryUsage
{
    val usage = """|Usage: java …Main <ZIP or JAR file containing class files>+
                  |(c) 2012 Ralf Mitschke (mitschke@st.informatik.tu-darmstadt.de)
                """.stripMargin

    final var instrumentation: Instrumentation = null

    def premain(options: String, inst: Instrumentation) {
        instrumentation = inst
        println (instrumentation.getObjectSize (new Object)) // 16
        println (instrumentation.getObjectSize (Array ())) // 24
        println (instrumentation.getObjectSize (Array (1))) // 32
        println (instrumentation.getObjectSize (1)) // 24
        println (instrumentation.getObjectSize (1L)) // 24
        println (instrumentation.getObjectSize ("")) // 40
        println (instrumentation.getObjectSize ("a")) // 40
        println (instrumentation.getObjectSize ("aa")) // 40
        println (instrumentation.getObjectSize ("aaa")) // 40
        val s = "aaaa"
        println (instrumentation.getObjectSize (s)) // 40
        val c1 = Array ('a', 'a', 'a', 'a')
        val c2 = Array ('a', 'a', '%', 'a', '%', 'a', '%', 'a', 'a')

        println (instrumentation.getObjectSize (c1)) // 32
        println (instrumentation.getObjectSize (c2)) // 48
        val ao0 = new Array[Object](0)
        val ao1 = new Array[Object](1)
        val ao3 = new Array[Object](3)
        val ao4 = new Array[Object](4)
        val ao5 = new Array[Object](5)
        println (instrumentation.getObjectSize (ao0)) // 48
        println (instrumentation.getObjectSize (ao1)) // 48
        println (instrumentation.getObjectSize (ao3)) // 48
        println (instrumentation.getObjectSize (ao4)) // 48
        println (instrumentation.getObjectSize (ao5)) // 48
    }

    def main(args: Array[String]) {

        if (args.length == 0 || !args.forall (arg ⇒ arg.endsWith (".zip") || arg.endsWith (".jar"))) {
            println (usage)
            sys.exit (1)
        }

        val files = for (arg ← args) yield {
            val file = new java.io.File (arg)
            if (!file.canRead || file.isDirectory) {
                println ("The file: " + file + " cannot be read.")
                println (usage)
                sys.exit (1)
            }
            file
        }


        memory (l => println ((l / 1024) + " KB"))(measure(files))
        for (i <- 1 to 100) {

        }


        sys.exit (0)
    }

    def measure(files : Seq[java.io.File]) {
        val database = BATDatabaseFactory.create ()

        val classCounter = new ElementCounter[ClassDeclaration] {}
        val methodCounter = new ElementCounter[MethodDeclaration] {}
        val fieldCounter = new ElementCounter[FieldDeclaration] {}
        //val o = new MemoryEstimateObserver()
        database.declared_classes.addObserver (classCounter)
        database.declared_methods.addObserver (methodCounter)
        database.declared_fields.addObserver (fieldCounter)


        for (file <- files) {
            memory (l => println ((l / 1024) + " KB"))(database.addArchive (new FileInputStream (file)))

        }
        //println((o.estimate / 1024) + " KB")
        println ("classes: " + classCounter.count)
        println ("methods: " + methodCounter.count)
        println ("fields: " + fieldCounter.count)
    }
}