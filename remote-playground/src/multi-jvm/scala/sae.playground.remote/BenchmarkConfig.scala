package sae.playground.remote

object BenchmarkConfig extends akka.remote.testkit.MultiNodeConfig {
  val nodes = List.tabulate(10)(x => role(s"node$x"))
}