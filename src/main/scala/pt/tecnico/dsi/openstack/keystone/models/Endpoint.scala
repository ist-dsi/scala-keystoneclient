package pt.tecnico.dsi.openstack.keystone.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.{Decoder, Encoder}
import io.circe.derivation.{deriveDecoder, deriveEncoder, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object Endpoint {
  object Create {
    implicit val encoder: Encoder[Create] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  /**
   * Options to create an Endpoint
   *
   * @param interface The interface type, which describes the visibility of the endpoint. Value is:
   *                  - public. Visible by end users on a publicly available network interface.
   *                  - internal. Visible by end users on an unmetered internal network interface.
   *                  - admin. Visible by administrative users on a secure network interface.
   * @param url The endpoint URL.
   * @param serviceId The UUID of the service to which the endpoint belongs.
   * @param regionId The ID of the region that contains the service endpoint.
   * @param enabled Defines whether the endpoint appears in the service catalog.
   */
  case class Create(
    interface: Interface,
    url: String, // Cannot be Uri because some urls contain interpolations, eg: "http://0.0.0.0:6007/v1/AUTH_%(tenant_id)s"
    serviceId: String,
    regionId: String,
    enabled: Boolean = true,
  )

  object Update {
    implicit val encoder: Encoder[Update] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  /**
   * Options to update an Endpoint
   *
   * @param interface The new interface type, which describes the visibility of the endpoint. Value is:
   *                  - public. Visible by end users on a publicly available network interface.
   *                  - internal. Visible by end users on an unmetered internal network interface.
   *                  - admin. Visible by administrative users on a secure network interface.
   * @param url The new endpoint URL.
   * @param serviceId The new endpoint service id.
   * @param regionId The new endpoint region id.
   * @param enabled The new enabled setting for the endpoint.
   */
  case class Update(
    interface: Option[Interface] = None,
    url: Option[String] = None, // Cannot be Uri because some urls contain interpolations, eg: "http://0.0.0.0:6007/v1/AUTH_%(tenant_id)s"
    serviceId: Option[String] = None,
    regionId: Option[String] = None,
    enabled: Option[Boolean] = None,
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(interface, url, serviceId, regionId, enabled).exists(_.isDefined)
    }
  }
  
  implicit val decoder: Decoder[Endpoint] = deriveDecoder(renaming.snakeCase)
  implicit val show: ShowPretty[Endpoint] = derived.semiauto.showPretty
}
final case class Endpoint(
  id: String,
  interface: Interface,
  regionId: String,
  url: String, // Cannot be Uri because some urls contain interpolations, eg: "http://0.0.0.0:6007/v1/AUTH_%(tenant_id)s"
  serviceId: String,
  enabled: Boolean = true,
  links: List[Link] = List.empty,
) extends Identifiable