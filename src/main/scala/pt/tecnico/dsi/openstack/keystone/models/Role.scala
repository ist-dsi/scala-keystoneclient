package pt.tecnico.dsi.openstack.keystone.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.{Codec, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object Role {
  object Create {
    implicit val encoder: Encoder[Create] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  /**
   * Options to create a Role
   *
   * @param name The role name.
   * @param description The description of the role.
   * @param domainId The ID of the domain of the role.
   */
  case class Create(
    name: String,
    description: Option[String] = None,
    domainId: Option[String] = None,
  )
  
  object Update {
    implicit val encoder: Encoder[Update] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  /**
   * Options to update a Role
   *
   * @param name TThe new role name.
   * @param description The new role description.
   */
  case class Update(
    name: Option[String] = None,
    description: Option[String] = None,
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(name, description).exists(_.isDefined)
    }
  }
  
  implicit val codec: Codec[Role] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Role] = derived.semiauto.showPretty
}
final case class Role(
  id: String,
  name: String,
  description: Option[String],
  domainId: Option[String],
  links: List[Link] = List.empty,
) extends Identifiable