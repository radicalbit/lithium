package akka.cluster.sbr

import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.MemberStatus.Up
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.remote.transport.ThrottlerTransportAdapter.Direction
import akka.testkit.ImplicitSender
import org.scalatest.concurrent.{Eventually, IntegrationPatience}

import scala.concurrent.duration._

abstract class FiveNodeSpec(name: String, config: FiveNodeSpecConfig)
    extends MultiNodeSpec(config)
    with STMultiNodeSpec
    with ImplicitSender
    with Eventually
    with IntegrationPatience {
  import config._

  override def initialParticipants: Int = roles.size

  private val addresses: Map[RoleName, Address] = roles.map(r => r -> node(r).address).toMap

  private def addressOf(roleName: RoleName): Address = addresses(roleName)

  s"$name" - {
    "1 - Start the 1st node" in within(30 seconds) {
      runOn(node1) {
        Cluster(system).join(addressOf(node1))
        waitForUp(node1)
      }

      enterBarrier("node1-up")
    }

    "2 - Start the 2nd node" in within(30 seconds) {
      runOn(node2) {
        Cluster(system).join(addressOf(node1))
        waitForUp(node1, node2)
      }

      enterBarrier("node2-up")
    }

    "3 - Start the 3rd node" in within(30 seconds) {
      runOn(node3) {
        Cluster(system).join(addressOf(node1))
        waitForUp(node1, node2, node3)
      }

      enterBarrier("node3-up")
    }

    "4 - Start the 4th node" in within(30 seconds) {
      runOn(node4) {
        Cluster(system).join(addressOf(node1))
        waitForUp(node1, node2, node3, node4)
      }

      enterBarrier("node4-up")
    }

    "5 - Start the 5th node" in within(60 seconds) {
      runOn(node5) {
        Cluster(system).join(addressOf(node1))
        waitForUp(node1, node2, node3, node4, node5)
      }

      enterBarrier("node5-up")
    }

    "6 - Bidirectional link failure" in within(60 seconds) {
      runOn(node1) {
        // Partition of node3, node4, and node 5.
        // Partition of node1 and node2.
        testConductor.blackhole(node3, node2, Direction.Both).await
        testConductor.blackhole(node1, node3, Direction.Both).await
        testConductor.blackhole(node2, node4, Direction.Both).await
        testConductor.blackhole(node2, node5, Direction.Both).await
        testConductor.blackhole(node1, node4, Direction.Both).await
        testConductor.blackhole(node1, node5, Direction.Both).await
      }

      enterBarrier("links-failed")

      runOn(node3, node4, node5) {
        waitForUp(node3, node4, node5)
        waitToBecomeUnreachable(node1, node2)
      }

      enterBarrier("node1-2-unreachable")

      runOn(node1, node2) {
        waitForUp(node1, node2)
        waitToBecomeUnreachable(node3, node4, node5)
      }

      enterBarrier("node-3-4-5-unreachable")

      runOn(node1, node2) {
        waitForUnreachableHandling()
        waitForSurvivors(node1, node2)
      }

      enterBarrier("node-3-4-5-downed")
    }
  }

  private def waitToBecomeUnreachable(roleNames: RoleName*): Unit = roleNames.map(addressOf).foreach { address =>
    awaitCond(Cluster(system).state.unreachable.exists(_.address == address))
  }

  private def waitForUnreachableHandling(): Unit =
    awaitCond(Cluster(system).state.unreachable.isEmpty)

  private def waitForSurvivors(roleNames: RoleName*): Unit = roleNames.map(addressOf).foreach { address =>
    awaitCond(Cluster(system).state.members.exists(_.address == address))
  }

  private def waitForUp(roleNames: RoleName*): Unit = roleNames.map(addressOf).foreach { address =>
    awaitCond(Cluster(system).state.members.exists(m => m.address == address && m.status == Up))
  }
}