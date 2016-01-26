package idb.remote

import akka.actor.{ActorRef, Actor}
import idb.observer.Observable

object ObservableHost {
  sealed trait HostMessage
  case class HostObservableAndForward[T](obs: Observable[T], target: ActorRef)/*(implicit pickler: Pickler[T])*/ extends HostMessage
  case class Forward(target: ActorRef) extends HostMessage
  //case class DoIt[T](fun: Observable[T] => Unit) extends HostMessage


}

class ObservableHost[T](var hosted: Option[Observable[T]] = None) extends Actor {
  import idb.remote.ObservableHost._

  override def receive = {
    case HostObservableAndForward(obs: Observable[T], target) =>
      println("Now hosting "+ obs.asInstanceOf[idb.Relation[T]].prettyprint(""))
      println(s"Target: ${target.toString()}, Self: ${context.self}")
      //target ! "Creating observer on remote host"
      obs.addObserver(new SendToRemote(target))
      println("Created new observer on remote host")
      hosted = Some(obs)

    case Forward(target) =>
      hosted.get.addObserver(new SendToRemote(target));

    /*case DoIt(fun: (Observable[T] => Unit)) =>
      fun(hosted.get)*/
  }

  def this(observable: Observable[T]) {
    this(Some(observable))
  }

  def this() {
    this(None)
  }
}