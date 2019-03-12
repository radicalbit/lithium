package akka.cluster.sbr

import akka.cluster.sbr.ArbitraryInstances._

class ReachableNodeSpec extends MySpec {
  "ReachableNode" - {
    "1 - should not affect the order" in {
      forAll { reachableNodes: List[ReachableNode] =>
        reachableNodes.sorted.map(_.node) shouldBe reachableNodes.map(_.node).sorted
      }
    }
  }
}