package pt.tecnico.dsi.keystone

import cats.effect.IO
import pt.tecnico.dsi.keystone.models.Region

class RegionSpec extends CRUDSpec[Region]("region", _.regions) {
  def stub = IO.pure(Region(
    description = "region description"
  ))
}