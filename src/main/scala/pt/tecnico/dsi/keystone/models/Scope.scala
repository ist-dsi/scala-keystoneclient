package pt.tecnico.dsi.keystone.models

import enumeratum.{Enum, EnumEntry}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.derivation.{deriveEncoder, deriveDecoder}

sealed trait Scope extends EnumEntry
case object Scope extends Enum[Scope] {
  case class System(all: Boolean = true) extends Scope
  object Domain {
    def id(id: String): Domain = Domain(id, null)
    def name(name: String): Domain = Domain(null, name)

    def fromEnvironment(env: Map[String, String], prefix: String = "OS"): Option[Domain] = {
      val idOpt = env.get(s"${prefix}_DOMAIN_ID") orElse env.get("OS_DEFAULT_DOMAIN_ID") orElse env.get("OS_DEFAULT_DOMAIN")
      val nameOpt = env.get(s"${prefix}_DOMAIN_NAME") orElse env.get("OS_DEFAULT_DOMAIN_NAME")
      (idOpt, nameOpt) match {
        case (Some(id), Some(name)) => Some(Domain(id, name))
        case (Some(id), None) => Some(Domain.id(id))
        case (None, Some(name)) => Some(Domain.name(name))
        case _ => None
      }
    }
  }
  case class Domain(id: String, name: String) extends Scope
  object Project {
    def apply(id: String): Project = new Project(id, null, null)
    def apply(name: String, domain: Domain): Project = new Project(null, name, domain)

    def fromEnvironment(env: Map[String, String]): Option[Project] = {
      val idOpt = env.get("OS_PROJECT_ID").map(id => Project(id))
      val nameOpt = env.get("OS_PROJECT_NAME").zip(Domain.fromEnvironment(env, "OS_PROJECT"))
        .map { case (name, domain) => Project(name, domain) }
      idOpt orElse nameOpt
    }
  }
  case class Project(id: String, name: String, domain: Domain) extends Scope
  case object Unscoped extends Scope

  def fromEnvironment(env: Map[String, String]): Option[Scope] = {
    val systemOpt = env.get("OS_SYSTEM_SCOPE").map(value => System(value.toLowerCase == "all"))
    val domainOpt = Domain.fromEnvironment(env)
    val projectOpt = Project.fromEnvironment(env)
    systemOpt orElse domainOpt orElse projectOpt
  }

  implicit val decoderSystem: Decoder[System] = deriveDecoder[System].at("system")
  implicit val decoderDomain: Decoder[Domain] = deriveDecoder[Domain].at("domain")
  implicit val decoderProject: Decoder[Project] = deriveDecoder[Project].at("project")
  implicit val decoder: Decoder[Scope] = { cursor: HCursor =>
    // If we cannot decode to a System|Domain|Project Scope then it is the Unscoped by definition
    decoderSystem(cursor) orElse decoderDomain(cursor) orElse decoderProject(cursor) orElse Right(Unscoped)
  }

  implicit val encoderSystem: Encoder[System] = (system: System) => deriveEncoder[System].apply(system)
  implicit val encoderDomain: Encoder[Domain] = (domain: Domain) => Json.obj(
    "id" -> Option(domain.id).asJson,
    "name" -> Option(domain.name).asJson
  ).dropNullValues
  implicit val encoderProject: Encoder[Project] = (project: Project) => Json.obj(
    "id" -> Option(project.id).asJson,
    "name" -> Option(project.name).asJson,
    "domain" -> Option(project.domain).asJson
  ).dropNullValues
  implicit val encoderUnscoped: Encoder[Unscoped.type] = (_: Unscoped.type) => "unscoped".asJson
  implicit val encoder: Encoder[Scope] = {
    case system: System => Json.obj("system" -> system.asJson)
    case domain: Domain => Json.obj("domain" -> domain.asJson)
    case project: Project => Json.obj("project" -> project.asJson)
    case unscoped: Unscoped.type => unscoped.asJson
  }

  val values = findValues
}