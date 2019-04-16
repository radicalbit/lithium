package akka.cluster.sbr.scenarios

import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.sbr.ArbitraryInstances._
import akka.cluster.sbr.{Node, ReachableNode, WorldView}
import cats.data.{NonEmptyList, NonEmptySet}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.scalacheck.Arbitrary

final case class SymmetricSplitScenario(worldViews: NonEmptyList[WorldView], clusterSize: Int Refined Positive)

object SymmetricSplitScenario {

  /**
   * Generates symmetric split scenarios where the allNodes is split
   * in multiple sub-clusters and where each one sees the rest as
   * unreachable.
   */
  implicit val arbSplitScenario: Arbitrary[SymmetricSplitScenario] = Arbitrary {

    def partitionedWorldView[N <: Node](nodes: NonEmptySet[N])(partition: NonEmptySet[N]): WorldView = {
      val otherNodes = nodes -- partition

      val worldView0 = new WorldView(ReachableNode(partition.head.member), partition.tail.map(identity), false)

      otherNodes.foldLeft[WorldView](worldView0) {
        case (worldView, node) => worldView.reachabilityEvent(UnreachableMember(node.member))
      }
    }

    for {
      nodes <- arbNonEmptySet[ReachableNode].arbitrary

      // Split the allNodes in `nSubCluster`.
      partitions <- splitCluster(nodes)

      // Each sub-allNodes sees the other nodes as unreachable.
      partitionedWorldViews = partitions.map(partitionedWorldView(nodes))
    } yield SymmetricSplitScenario(partitionedWorldViews, refineV[Positive](nodes.length).right.get)
  }

}
