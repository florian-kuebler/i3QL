import sbt._

object sae extends Build {

  /*
    Project IDB
  */
	lazy val ibd = Project(id = "idb", base = file("idb"))
		.aggregate (runtime, annotations, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax)

	lazy val runtime = Project(id = "idb-runtime", base = file("idb/runtime"))

	lazy val annotations = Project(id = "idb-annotations", base = file("idb/annotations"))

	lazy val intermediateRepresentation = Project(id = "idb-intermediate-representation", base = file("idb/intermediate-representation"))
		.dependsOn (annotations % "compile")

	lazy val schemaExamples = Project(id = "idb-schema-examples", base = file("idb/schema-examples"))
		.dependsOn (annotations % "compile")

	lazy val runtimeCompiler = Project(id = "idb-runtime-compiler", base = file("idb/runtime-compiler"))
		.dependsOn (schemaExamples % "compile;test->compile", runtime % "compile", intermediateRepresentation % "compile")

	lazy val syntax = Project(id = "idb-syntax-iql", base = file("idb/syntax-iql"))
		.dependsOn (runtimeCompiler % "compile", schemaExamples % "compile;test->compile")

	lazy val integrationTest = Project(id = "idb-integration-test", base = file("idb/integration-test"))
		.dependsOn (schemaExamples % "compile;test->compile", syntax % "compile")
		
	/*
    Project Bytecode Database
	*/
	lazy val db = Project(id = "db", base = file("bytecode-database"))
		.aggregate (databaseInterface, bindingASM)

  lazy val databaseInterface = Project(id = "db-interface", base = file("bytecode-database/interface"))
    .dependsOn (syntax % "compile")

	lazy val bindingASM = Project(id = "db-binding-asm", base = file("bytecode-database/binding-asm"))
    .dependsOn (databaseInterface % "compile", testData % "compile;test->compile")
    
  /*
    Project Analyses
  */

  lazy val analyses = Project(id = "analyses", base = file("analyses"))
    .aggregate (findbugs, metrics)
  
  lazy val findbugs = Project(id = "analyses-findbugs", base = file("analyses/findbugs"))
    .dependsOn(databaseInterface % "compile", bindingASM % "compile", testData % "compile;test->compile")
  
  lazy val metrics = Project(id = "analyses-metrics", base = file("analyses/metrics"))
    .dependsOn(databaseInterface % "compile", bindingASM % "compile", testData % "compile;test->compile")
   
  /*
    Project Test Data
  */ 
  
  lazy val testData = Project(id = "test-data", base = file("test-data"))
   
  /*
    Root Project
  */  
  lazy val root = Project(id = "sae", base = file("."))
		.aggregate (runtime, annotations, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax, databaseInterface, bindingASM, findbugs, metrics)
  
    

	

  val virtScala = "2.10.2-RC1"

}
