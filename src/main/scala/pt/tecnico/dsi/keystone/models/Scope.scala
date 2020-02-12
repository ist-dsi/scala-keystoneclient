package pt.tecnico.dsi.keystone.models

import io.circe._
import io.circe.syntax._

sealed trait Scope
object Scope {
  case class System(all: Boolean = true) extends Scope
  case class Domain(domain: auth.Domain) extends Scope
  case class Project(project: auth.Project) extends Scope
  case object Unscoped extends Scope

  def apply(domain: auth.Domain): Scope = Domain(domain)
  def apply(project: auth.Project): Scope = Project(project)

  implicit val decoderSystem: Decoder[System] = (c: HCursor) => c.downField("system").get[Boolean]("all").map(System.apply)
  implicit val decoderDomain: Decoder[Domain] = (c: HCursor) => c.get[auth.Domain]("domain").map(Domain.apply)
  implicit val decoderProject: Decoder[Project] = (c: HCursor) => c.get[auth.Project]("project").map(Project.apply)

  implicit val decoder: Decoder[Scope] = { cursor: HCursor =>
    // If we cannot decode to a System|Domain|Project Scope then it is the Unscoped Domain by definition
    decoderSystem(cursor) orElse decoderDomain(cursor) orElse decoderProject(cursor) orElse Right(Unscoped)
  }
  implicit val encoder: Encoder[Scope] = {
    case System(all) => Json.obj("system" -> Json.obj("all" -> all.asJson))
    case Domain(domain) => Json.obj("domain" -> domain.asJson)
    case Project(project) => Json.obj("project" -> project.asJson)
    case Unscoped => "unscoped".asJson
  }
}