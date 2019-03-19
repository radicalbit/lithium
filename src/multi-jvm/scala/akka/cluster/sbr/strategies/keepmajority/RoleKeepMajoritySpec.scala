package akka.cluster.sbr.strategies.keepmajority

import akka.cluster.sbr.FiveNodeSpec

class RoleKeepMajoritySpecMultiJvmNode1 extends RoleKeepMajoritySpec
class RoleKeepMajoritySpecMultiJvmNode2 extends RoleKeepMajoritySpec
class RoleKeepMajoritySpecMultiJvmNode3 extends RoleKeepMajoritySpec
class RoleKeepMajoritySpecMultiJvmNode4 extends RoleKeepMajoritySpec
class RoleKeepMajoritySpecMultiJvmNode5 extends RoleKeepMajoritySpec

class RoleKeepMajoritySpec extends FiveNodeSpec("KeepMajority", RoleKeepMajoritySpecConfig)
