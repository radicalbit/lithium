package akka.cluster.sbr

import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.prop.PropertyChecks

trait MySpec extends FreeSpec with Matchers with PropertyChecks {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfig(minSuccessful = 100, maxDiscarded = 5000, maxSize = 100, workers = 8)
//    PropertyCheckConfig(minSuccessful = 1000, maxDiscarded = 50000, maxSize = 100, workers = 8)
//    PropertyCheckConfig(minSuccessful = 5000, maxDiscarded = 250000, maxSize = 100, workers = 8)
//    PropertyCheckConfig(minSuccessful = 100)
}