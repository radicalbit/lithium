package akka.cluster.sbr.strategies.staticquorum

import akka.cluster.sbr.FiveNodeSpecConfig
import com.typesafe.config.ConfigFactory

object RoleStaticQuorumSpecConfig extends FiveNodeSpecConfig("role_static_quorum_spec.conf") {
  nodeConfig(node1, node2, node3)(ConfigFactory.parseString("""akka.cluster.roles = ["foo"]"""))
}
