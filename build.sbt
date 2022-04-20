// Note: settings common to all subprojects are defined in project/GlobalPlugin.scala

// The root project is implicit, so we don't have to define it.
// We do need to prevent publishing for it, though:

import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val assemblySettings = Seq(
  assembly / assemblyShadeRules := Seq(
    ShadeRule.rename("org.apache.avro.**" -> "shadedAvro.@1").inAll
  ),
  assembly / test := {},
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.last
    case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
    case PathList("codegen", "config.fmpp") => MergeStrategy.last
    case PathList("dev", "ludovic", "netlib", "InstanceBuilder.class") => MergeStrategy.last
    case PathList("git.properties") => MergeStrategy.last
    case PathList("io", "netty", _*) => MergeStrategy.last
    case PathList("log4j.properties") => MergeStrategy.last
    case PathList("mozilla", "public-suffix-list.txt") => MergeStrategy.last
    case PathList("scala", "annotation", "nowarn.class" | "nowarn$.class") => MergeStrategy.last
    case PathList(ps@_*) if ps.contains("native-image.properties") => MergeStrategy.last
    case PathList(ps@_*) if ps.contains("reflection-config.json") => MergeStrategy.last
    case PathList(ps@_*) if ps.last == "module-info.class" => MergeStrategy.discard
    case PathList(ps@_*) if ps.last.endsWith(".proto") => MergeStrategy.last
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

//lazy val sbtPublishing = assembly / artifact := {
//  val art = (assembly / artifact).value
//  art.withClassifier(Some("assembly"))
//}
//
//lazy val uberJar = project
//  .enablePlugins(AssemblyPlugin)
//  .settings(
//      publish / skip := true
//)

lazy val root = Project("avro4s", file("."))
  .settings(
    name := "avro4s",
////    Compile / packageBin := (uberJar / assembly).value,
////    assembly / aggregate := true,
////    publish := {},
////    publishArtifact := false,
//    assemblySettings,
//    publishMavenStyle := true,
//    addArtifact(Artifact("avro4s-core", "assembly"), sbtassembly.AssemblyKeys.assembly),
////    sbtPublishing,
//    addArtifact(assembly / artifact, assembly),
//    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",
//        addArtifact(Artifact("avro4s", "assembly"), sbtassembly.AssemblyKeys.assembly),
//    releaseProcess := Seq[ReleaseStep](publishArtifacts),
//    publishConfiguration := publishConfiguration.value.withOverwrite(true),
//    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  )
  .aggregate(
    `avro4s-core`,
    `avro4s-json`,
   // `avro4s-cats`,
    `avro4s-kafka`,
    `avro4s-refined`
  )

val `avro4s-core` = project.in(file("avro4s-core"))
  .settings(
    //    Compile / packageBin := (uberJar / assembly).value,
    //    assembly / aggregate := true,
    //    publish := {},
    //    publishArtifact := false,
    assemblySettings,
    publishMavenStyle := true,
    addArtifact(Artifact("avro4s-core", "assembly"), sbtassembly.AssemblyKeys.assembly),
    //    sbtPublishing,
    addArtifact(assembly / artifact, assembly),
    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",
    addArtifact(Artifact("avro4s", "assembly"), sbtassembly.AssemblyKeys.assembly),
    releaseProcess := Seq[ReleaseStep](publishArtifacts),
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    assemblySettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.magnolia" %% "magnolia-core" % MagnoliaVersion,
      "com.chuusai" %% "shapeless" % ShapelessVersion,
      "org.json4s" %% "json4s-native" % Json4sVersion
    )
  )

val `avro4s-json` = project.in(file("avro4s-json"))
  .dependsOn(`avro4s-core`)
  .settings(
    assemblySettings,
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % Json4sVersion
    )
  )

val `avro4s-cats` = project.in(file("avro4s-cats"))
  .dependsOn(`avro4s-core`)
  .settings(
    assemblySettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion
    )
  )

val `avro4s-kafka` = project.in(file("avro4s-kafka"))
  .dependsOn(`avro4s-core`)
  .settings(
    assemblySettings,
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "2.4.0"
    )
  )

val `avro4s-refined` = project.in(file("avro4s-refined"))
  .dependsOn(`avro4s-core` % "compile->compile;test->test")
  .settings(
    assemblySettings,
    libraryDependencies ++= Seq(
      "eu.timepit" %% "refined" % RefinedVersion
    )
  )

val benchmarks = project
  .in(file("benchmarks"))
  .dependsOn(`avro4s-core`)
  .enablePlugins(JmhPlugin)
  .settings(
    assemblySettings,
    libraryDependencies ++= Seq(
      "pl.project13.scala" % "sbt-jmh-extras" % SbtJmhVersion,
      "org.openjdk.jmh" % "jmh-core" % JmhVersion,
      "org.openjdk.jmh" % "jmh-generator-asm" % JmhVersion,
      "org.openjdk.jmh" % "jmh-generator-bytecode" % JmhVersion,
      "org.openjdk.jmh" % "jmh-generator-reflection" % JmhVersion
    )
  )