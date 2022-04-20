import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys.{assemblyJarName, assemblyMergeStrategy, assemblyShadeRules}
import sbtassembly.AssemblyPlugin.autoImport.{MergeStrategy, ShadeRule, assembly}
import sbtassembly.PathList
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseProcess}
import sbtrelease.ReleaseStateTransformations.publishArtifacts

/** Adds common settings automatically to all subprojects */
object Build extends AutoPlugin {

  object autoImport {
    val org = "com.sksamuel.avro4s"
    val AvroVersion = "1.9.2"
    val Log4jVersion = "1.2.17"
    val ScalatestVersion = "3.2.9"
    val Slf4jVersion = "1.7.30"
    val Json4sVersion = "3.6.11"
    val CatsVersion = "2.0.0"
    val RefinedVersion = "0.9.26"
    val ShapelessVersion = "2.3.7"
    val MagnoliaVersion = "1.0.0-M4"
    val SbtJmhVersion = "0.3.7"
    val JmhVersion = "1.23"
  }

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


  import autoImport._

  def publishVersion = "foo2-unity-SNAPSHOT"

  override def trigger = allRequirements
  override def projectSettings = assemblySettings ++ publishingSettings ++ Seq(
    organization := org,
    scalaVersion := "2.13.5",
    crossScalaVersions := Seq("2.12.14", "2.13.5"),
    resolvers += Resolver.mavenLocal,
    Test / parallelExecution  := false,
    scalacOptions := Seq(
      "-unchecked", "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-language:higherKinds",
      "-language:existentials",
      "-Ybackend-parallelism",
      "8"
    ),
    javacOptions := Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      "org.scala-lang"    % "scala-reflect"     % scalaVersion.value,
      "org.scala-lang"    % "scala-compiler"    % scalaVersion.value,
      "org.apache.avro"   % "avro"              % AvroVersion,
      "org.slf4j"         % "slf4j-api"         % Slf4jVersion          % "test",
      "log4j"             % "log4j"             % Log4jVersion          % "test",
      "org.slf4j"         % "log4j-over-slf4j"  % Slf4jVersion          % "test",
      "org.scalatest"     %% "scalatest"        % ScalatestVersion      % "test"
    )
  )

//  lazy val uberJar = project
//    .enablePlugins(AssemblyPlugin)
//    .settings(
//      publish / skip := true
//    )
//
//  lazy val sbtPublishing = assembly / artifact := {
//    val art = (assembly / artifact).value
//    art.withClassifier(Some("assembly"))
//  }



  val publishingSettings = Seq(
    publishMavenStyle := true,
    Test / publishArtifact := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    version := publishVersion,
    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",
    publishTo := Some(
//      Resolver.mavenLocal
      "Artifactory Realm" at "https://unity3d.jfrog.io/artifactory/ai-data-liquidus-sbt"
    ),
//    Compile / packageBin := (uberJar / assembly).value,
    //    publish := {},
    //    publishArtifact := false,
    publishMavenStyle := true,
    assembly / assemblyJarName := s"${name.value}-${version.value}-assembly.jar",
    //    addArtifact(Artifact("avro4s", "assembly"), sbtassembly.AssemblyKeys.assembly),
    releaseProcess := Seq[ReleaseStep](publishArtifacts),
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    pomExtra := {
      <url>https://github.com/sksamuel/avro4s</url>
        <licenses>
          <license>
            <name>MIT</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:sksamuel/avro4s.git</url>
          <connection>scm:git@github.com:sksamuel/avro4s.git</connection>
        </scm>
        <developers>
          <developer>
            <id>sksamuel</id>
            <name>sksamuel</name>
            <url>http://github.com/sksamuel</url>
          </developer>
        </developers>
    }
  )
}
