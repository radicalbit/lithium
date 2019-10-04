resolvers += "SwissBorg Nexus".at("https://nexus.sharedborg.com/repository/investmentapp-mvn/")

addSbtPlugin("io.spray"                % "sbt-revolver"    % "0.9.1")
addSbtPlugin("com.typesafe.sbt"        % "sbt-multi-jvm"   % "0.4.0")
addSbtPlugin("org.scalameta"           % "sbt-scalafmt"    % "2.0.4")
addSbtPlugin("ch.epfl.scala"           % "sbt-scalafix"    % "0.9.4")
addSbtPlugin("org.scoverage"           % "sbt-scoverage"   % "1.5.1")
addSbtPlugin("org.wartremover"         % "sbt-wartremover" % "2.4.2")