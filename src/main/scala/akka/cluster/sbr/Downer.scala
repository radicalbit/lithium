package akka.cluster.sbr

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.sbr.strategies.downall.DownAll
import akka.cluster.sbr.strategies.indirected.Indirected
import akka.cluster.sbr.strategy.Strategy
import akka.cluster.sbr.strategy.ops._

import scala.concurrent.duration._

class Downer[A: Strategy](cluster: Cluster,
                          strategy: A,
                          stableAfter: FiniteDuration,
                          downAllWhenUnstable: FiniteDuration)
    extends Actor
    with ActorLogging {

  import Downer._

  private val _ = context.system.actorOf(StabilityReporter.props(self, stableAfter, downAllWhenUnstable, cluster))

  override def receive: Receive = {
    case h @ HandleIndirectlyConnected(worldView) =>
      log.debug("{}", h)

//      if (cluster.state.leader.contains(cluster.selfAddress)) {
      Indirected
        .takeDecision(worldView)
        .toTry
        .map(execute)
        .get
//      }

    case h @ HandleSplitBrain(worldView) =>
      log.debug("{}", h)

//      if (cluster.state.leader.contains(cluster.selfAddress)) {
      strategy
        .takeDecision(worldView)
        .toTry
        .map(execute)
        .get
//      }

//    case ClusterIsUnstable(worldView) =>
//      log.debug("Cluster is unstable.")
//      DownAll.takeDecision(worldView).toTry.map(execute).get
  }

  private def execute(decision: StrategyDecision): Unit = {
    log.debug("Executing decision: {}", decision.clean)
    decision.nodesToDown.foreach(node => cluster.down(node.member.address))
  }
}

object Downer {
  def props[A: Strategy](cluster: Cluster,
                         strategy: A,
                         stableAfter: FiniteDuration,
                         downAllWhenUnstable: FiniteDuration): Props =
    Props(new Downer(cluster, strategy, stableAfter, downAllWhenUnstable))

  final case class HandleSplitBrain(worldView: WorldView)
//  final case class ClusterIsUnstable(worldView: WorldView)
  final case class HandleIndirectlyConnected(worldView: WorldView)
}
