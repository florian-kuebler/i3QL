package sae.playground.remote

import akka.actor
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorPath, Props}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import idb.BagTable
import idb.observer.Observer
import idb.remote._

class BenchmarkTestMultiJvmNode1 extends BenchmarkTest
class BenchmarkTestMultiJvmNode2 extends BenchmarkTest
class BenchmarkTestMultiJvmNode3 extends BenchmarkTest
class BenchmarkTestMultiJvmNode4 extends BenchmarkTest
class BenchmarkTestMultiJvmNode5 extends BenchmarkTest
class BenchmarkTestMultiJvmNode6 extends BenchmarkTest
class BenchmarkTestMultiJvmNode7 extends BenchmarkTest
class BenchmarkTestMultiJvmNode8 extends BenchmarkTest
class BenchmarkTestMultiJvmNode9 extends BenchmarkTest
class BenchmarkTestMultiJvmNode10 extends BenchmarkTest
object BenchmarkTest {} // this object is necessary for multi-node testing

class BenchmarkTest extends MultiNodeSpec(BenchmarkConfig)
with STMultiNodeSpec with ImplicitSender {

  type D = Long

  import BenchmarkConfig._
  import BenchmarkTest._

  def initialParticipants = roles.size

  "A RemoteView" must {
    "receive from a simple ObservableHost" in {
      runOn(nodes(0)) {
        val db = BagTable.empty[D]

        system.actorOf(Props(classOf[ObservableHost[D]], db), "db")

        val remoteHostPath = node(nodes(0)) / "user" / "db"

        var tree = RemoteView[D](system, remoteHostPath, false)

        for (i <- 1 to 9) {
          tree = RemoteView[D](system, node(nodes(i)).address, tree)
        }

        tree = RemoteView[D](system, node(nodes(0)).address, tree)

        ObservableHost.forward(tree, system)
        tree.addObserver(new Observer[D] {
          override def added(v: D) = {
            val currentTime = System.nanoTime()
            println(currentTime - v)
          }
          override def removed(v: D) = {}
          override def updated(oldV: D, newV: D) = {}
          override def addedAll(vs: Seq[D]) = {}
          override def removedAll(vs: Seq[D]) = {}
          override def endTransaction() = {}
        })

        tree.addObserver(new SendToRemote[D](testActor))

        Thread.sleep(100)

        val currentTime = System.nanoTime()

        db += currentTime

        import scala.concurrent.duration._
        expectMsg(50.seconds, Added(currentTime))


      }

      // needed to keep other hosts running
      enterBarrier("finished")
    }
  }
}