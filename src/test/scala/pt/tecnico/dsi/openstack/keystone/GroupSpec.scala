package pt.tecnico.dsi.openstack.keystone

import cats.effect.IO
import pt.tecnico.dsi.openstack.keystone.models.Group

class GroupSpec extends CrudSpec[Group]("group", _.groups) {
  def stub = IO.pure(Group(
    name = "test-group",
    description = "test-desc",
    domainId = "default"
  ))
}