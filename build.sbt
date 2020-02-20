organization := "pt.tecnico.dsi"
name := "scala-keystoneclient"

// ======================================================================================================================
// ==== Compile Options =================================================================================================
// ======================================================================================================================
javacOptions ++= Seq("-Xlint", "-encoding", "UTF-8", "-Dfile.encoding=utf-8")
scalaVersion := "2.13.1"

scalacOptions ++= Seq(
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Explicitly enables the implicit conversions feature
  "-Ybackend-parallelism", "4",        // Maximum worker threads for backend.
  "-Ybackend-worker-queue", "10",      // Backend threads worker queue size.
  "-Ymacro-annotations",               // Enable support for macro annotations, formerly in macro paradise.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xmigration:2.14.0",                // Warn about constructs whose behavior may have changed since version.
  "-Xfatal-warnings", "-Werror",       // Fail the compilation if there are any warnings.
  //"-Xlint:_",                          // Enables every warning. scalac -Xlint:help for a list and explanation
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-Wdead-code",                       // Warn when dead code is identified.
  "-Wextra-implicit",                  // Warn when more than one implicit parameter section is defined.
  "-Wnumeric-widen",                   // Warn when numerics are widened.
  "-Woctal-literal",                   // Warn on obsolete octal syntax.
  //"-Wself-implicit",                   // Warn when an implicit resolves to an enclosing self-definition.
  //"-Wunused:_",                        // Enables every warning of unused members/definitions/etc
  //"-Wunused:patvars",                  // Warn if a variable bound in a pattern is unused.
  //"-Wunused:params",                   // Enable -Wunused:explicits,implicits. Warn if an explicit/implicit parameter is unused.
  //"-Wunused:linted",                   // -Xlint:unused <=> Enable -Wunused:imports,privates,locals,implicits.
  //"-Wvalue-discard",                   // Warn when non-Unit expression results are unused.
)
// These lines ensure that in sbt console or sbt test:console the -Ywarn* and the -Xfatal-warning are not bothersome.
// https://stackoverflow.com/questions/26940253/in-sbt-how-do-you-override-scalacoptions-for-console-in-all-configurations
scalacOptions in (Compile, console) ~= (_.filterNot { option =>
  option.startsWith("-Ywarn") || option == "-Xfatal-warnings" || option.startsWith("-W") || option.startsWith("-Xlint")
})
scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

fork := true

// ======================================================================================================================
// ==== Dependencies ====================================================================================================
// ======================================================================================================================
libraryDependencies ++= Seq("blaze-client", "dsl", "circe").map { module =>
  "org.http4s"      %% s"http4s-$module" % "0.21.0-M6"
} ++ Seq(
  "io.circe"        %% "circe-derivation"  % "0.12.0-M7",
  "io.circe"        %% "circe-generic-extras"  % "0.12.2",
  "io.circe"        %% "circe-parser"  % "0.12.3",
  "ch.qos.logback"  %  "logback-classic" % "1.2.3" % Test,
  "org.scalatest"   %% "scalatest"       % "3.1.0" % Test,
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

Test / logBuffered := false
Test / fork := true
//coverageEnabled := true
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

// ======================================================================================================================
// ==== Scaladoc ========================================================================================================
// ======================================================================================================================
git.remoteRepo := s"git@github.com:ist-dsi/${name.value}.git"
git.useGitDescribe := true // Get version by calling `git describe` on the repository
val latestReleasedVersion = SettingKey[String]("latest released version")
latestReleasedVersion := git.gitDescribedVersion.value.getOrElse("0.0.1-SNAPSHOT")

// Define the base URL for the Scaladocs for your library. This will enable clients of your library to automatically
// link against the API documentation using autoAPIMappings.
apiURL := Some(url(s"${homepage.value.get}/api/${latestReleasedVersion.value}/"))
autoAPIMappings := true // Tell scaladoc to look for API documentation of managed dependencies in their metadata.
scalacOptions in (Compile, doc) ++= Seq(
  "-author",      // Include authors.
  "-diagrams",    // Create inheritance diagrams for classes, traits and packages.
  "-groups",      // Group similar functions together (based on the @group annotation)
  "-implicits",   // Document members inherited by implicit conversions.
  "-doc-title", name.value.capitalize,
  "-doc-version", latestReleasedVersion.value,
  "-doc-source-url", s"${homepage.value.get}/tree/v${latestReleasedVersion.value}€{FILE_PATH}.scala",
  "-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath,
)

enablePlugins(GhpagesPlugin, SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := s"api/${version.value}"
//includeFilter in ghpagesCleanSite := (p => p.toPath.toAbsolutePath.startsWith((ghpagesRepository.value / "api" / version.value).toPath.toAbsolutePath))
//excludeFilter in ghpagesCleanSite := (p => p.toPath.toAbsolutePath.startsWith((ghpagesRepository.value / "api").toPath.toAbsolutePath))
excludeFilter in ghpagesCleanSite := AllPassFilter // We want to keep the previous API versions, .gitlab-ci.yml, etc
val latestFileName = "latest"
val createLatestSymlink = taskKey[Unit](s"Creates a symlink named $latestFileName which points to the latest version.")
createLatestSymlink := {
  ghpagesSynchLocal.value // Ensure the ghpagesRepository already exists
  import java.nio.file.Files
  val path = (ghpagesRepository.value / "api" / latestFileName).toPath
  if (!Files.isSymbolicLink(path)) Files.createSymbolicLink(path, new File(latestReleasedVersion.value).toPath)
}
ghpagesPushSite := ghpagesPushSite.dependsOn(createLatestSymlink).value
ghpagesBranch := "pages"
ghpagesNoJekyll := false
envVars in ghpagesPushSite := Map("SBT_GHPAGES_COMMIT_MESSAGE" -> s"Add Scaladocs for version ${latestReleasedVersion.value}")

// ======================================================================================================================
// ==== Publishing/Release ==============================================================================================
// ======================================================================================================================
publishTo := sonatypePublishTo.value
sonatypeProfileName := organization.value

licenses += "MIT" -> url("http://opensource.org/licenses/MIT")
homepage := Some(url(s"https://github.com/ist-dsi/${name.value}"))
scmInfo := Some(ScmInfo(homepage.value.get, git.remoteRepo.value))
developers += Developer("Lasering", "Simão Martins", "", url("https://github.com/Lasering"))

// Will fail the build/release if updates for the dependencies are found
//dependencyUpdatesFailBuild := true

releaseUseGlobalVersion := false
releaseNextCommitMessage := s"Setting version to ${ReleasePlugin.runtimeVersion.value} [skip ci]"

releasePublishArtifactsAction := PgpKeys.publishSigned.value // Maven Central requires packages to be signed
import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  releaseStepTask(dependencyUpdates),
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepTask(Compile / doc),
  releaseStepTask(Test / test), // For this to work "docker run --cap-add IPC_LOCK -d --name=dev-vault -p 8200:8200 vault"
  setReleaseVersion,
  tagRelease,
  releaseStepTask(ghpagesPushSite),
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  pushChanges,
  setNextVersion
)
