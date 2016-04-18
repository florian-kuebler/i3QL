package sae.playground.remote

import akka.remote.testkit.MultiNodeConfig

object BenchmarkConfig extends MultiNodeConfig {
  val nodes = List.tabulate(10)(x => role(s"node$x"))
}