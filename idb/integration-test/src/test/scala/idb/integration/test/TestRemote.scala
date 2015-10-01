package idb.integration.test

import java.util
import java.util.Date

import idb.syntax.iql.IR._
import idb.{SetTable, Table, BagTable}
import idb.algebra.ir._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.annotations.RemoteHost
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.schema.university.{Registration, Student}
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import idb.syntax.iql._
import UniversityDatabase._

import scala.virtualization.lms.common.{TupledFunctionsExp, StaticDataExp, StructExp, ScalaOpsPkgExp}


/**
 * @author Mirko Köhler
 */
class TestRemote extends UniversityTestData {


	@Test
	def testRemote(): Unit = {
		@RemoteHost(description = "students")
		class RemoteStudents extends BagTable[Student]

		@RemoteHost(description = "registrations")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteStudents = new RemoteStudents
		val remoteRegistrations = new RemoteRegistrations

		val q = root(
			plan(
				SELECT (*) FROM (remoteStudents, remoteRegistrations)
			)
		)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")

		remoteStudents.add(johnDoe)
		remoteRegistrations.add(johnTakesEise)

		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}


	@Test
	def testRemote2(): Unit = {
		@RemoteHost(description = "students")
		class RemoteStudents extends BagTable[Student]

		@RemoteHost(description = "registrations")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteStudents = new RemoteStudents
		val remoteRegistrations = new RemoteRegistrations

		val q = root(
			plan(
				SELECT (*) FROM (remoteStudents, remoteStudents, remoteRegistrations, remoteStudents)
			)
		)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")

		remoteStudents.add(johnDoe)
		remoteRegistrations.add(johnTakesEise)

		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}

	@Test
	def testRemote3(): Unit = {
		@RemoteHost(description = "students")
		class RemoteStudents extends BagTable[Student]

		@RemoteHost(description = "registrations")
		class RemoteRegistrations extends BagTable[Registration]

		val remoteStudents = new RemoteStudents
		val remoteRegistrations = new RemoteRegistrations

		val q = root(
			plan(
				SELECT (COUNT(*))
					FROM (remoteStudents, remoteStudents, remoteRegistrations, remoteRegistrations)
					WHERE ((s1, s2, r1, r2) =>
						s1.matriculationNumber == r1.studentMatriculationNumber AND
						s2.matriculationNumber == r2.studentMatriculationNumber AND
						r1.courseNumber == r2.courseNumber
					)
					GROUP BY ((s1: Rep[Student], s2: Rep[Student], r1: Rep[Registration], r2 : Rep[Registration]) => r1.courseNumber)
			)
		)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")

		remoteStudents.add(sallyFields)
		remoteRegistrations.add(sallyTakesIcs1)
		remoteStudents.add(jackBlack)
		remoteRegistrations.add(jackTakesIcs1)

		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}

	@Test
	def testRemote4(): Unit = {
		@RemoteHost(description = "students")
		class RemoteStudents extends BagTable[Student]

		val remoteStudents = new RemoteStudents

		val q = root(
			plan(
				SELECT (COUNT(*))
				FROM remoteStudents
				WHERE (s1 =>
					s1.lastName == "Doe"
				)
			)
		)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")

		remoteStudents += johnDoe
		remoteStudents += sallyFields

		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}


	@Ignore
	@Test
	def testRemoteAirports(): Unit = {
		import idb.syntax.iql.IR._

		@RemoteHost(description = "airports")
		object RemoteAirports extends SetTable[Airport]

		@RemoteHost(description = "flights")
		object RemoteFlights extends SetTable[Flight]



		val q = plan(
			SELECT ((s: Rep[String]) => s,
				COUNT(*))
				FROM (RemoteAirports, RemoteAirports, RemoteFlights)
				WHERE ((a1, a2, f) =>
				f.from == a1.id AND
					f.to == a2.id AND
					a2.code == "PDX" AND
					f.takeoff >= new Date(2014, 1, 1) AND
					f.takeoff < new Date(2015, 1, 1))
				GROUP BY ((a1: Rep[Airport], a2: Rep[Airport], f: Rep[Flight]) => a1.city)
			)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		Predef.println(printer.quoteRelation(q))
		Predef.println("\n")
		Predef.println(compiledQ.prettyprint("\t"))
		Predef.println("\n")


		compiledQ.foreach(x => Predef.println(x))
		Predef.println("\n")
	}

}