package pt.tecnico.dsi.keystone.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Endpoint {
  implicit val codec: Codec.AsObject[Endpoint] = deriveCodec(renaming.snakeCase, false, None)
}
case class Endpoint(
  interface: Interface,
  regionId: String,
  url: String, // Cannot be Uri because some urls contain interpolations, eg: "http://0.0.0.0:6007/v1/AUTH_%(tenant_id)s"
  serviceId: String,
  enabled: Boolean = true,
) extends WithEnabled[Endpoint] {
  override def withEnabled(enabled: Boolean): Endpoint = copy(enabled = enabled)
}