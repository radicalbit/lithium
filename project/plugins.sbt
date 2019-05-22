resolvers += "SwissBorg Nexus".at("https://nexus.sharedborg.com/repository/investmentapp-mvn/")

addSbtPlugin("io.spray"         % "sbt-revolver"  % "0.9.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")
addSbtPlugin("com.geirsson"     % "sbt-scalafmt"  % "1.5.1")
addSbtPlugin("io.get-coursier"  % "sbt-coursier"  % "1.1.0-M11")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"  % "0.9.4")
addSbtPlugin("org.scoverage"    % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.swissborg"    % "sbt-swissborg" % "0.4.0")