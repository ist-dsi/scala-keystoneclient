package pt.tecnico.dsi.openstack.keystone.models

import cats.effect.Sync
import cats.instances.list._
import cats.syntax.foldable._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.derivation.{deriveDecoder, deriveEncoder, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.keystone.KeystoneClient

object Group {
  implicit val codec: Decoder[Group] = deriveDecoder(renaming.snakeCase)

  object Create {
    implicit val encoder: Encoder[Create] = deriveEncoder(renaming.snakeCase)
  }
  /**
   * Options to create a Group.
   * @param name The name of the group.
   * @param description The description of the group.
   * @param domainId The ID of the domain of the group. If the domain ID is not provided in the request, the Identity service will attempt
   *                 to pull the domain ID from the token used in the request. Note that this requires the use of a domain-scoped token.
   */
  case class Create(
    name: String,
    description: Option[String] = None,
    domainId: Option[String] = None,
  )

  object Update {
    implicit val encoder: Encoder[Update] = deriveEncoder(renaming.snakeCase)
  }
  /**
   * Options to update a Group.
   * @param name The new name of the group.
   * @param description The new description of the group.
   */
  case class Update(
    name: Option[String] = None,
    description: Option[String] = None,
  )
}
final case class Group(
  id: String,
  name: String,
  description: Option[String] = None,
  domainId: String,
  links: List[Link] = List.empty,
) extends Identifiable { self =>
  def domain[F[_]](implicit client: KeystoneClient[F]): F[Domain] = client.domains.apply(domainId) // The domain must exist

  def users[F[_]: Sync](implicit client: KeystoneClient[F]): Stream[F, User] = client.groups.listUsers(self.id)
  def addUser[F[_]: Sync](id: String)(implicit client: KeystoneClient[F]): F[Unit] = client.groups.addUser(self.id, id)
  def addUsers[F[_]: Sync](ids: List[String])(implicit client: KeystoneClient[F]): F[Unit] = ids.traverse_(client.groups.addUser(self.id, _))
  def removeUser[F[_]: Sync](id: String)(implicit client: KeystoneClient[F]): F[Unit] = client.groups.removeUser(self.id, id)
  def removeUsers[F[_]: Sync](ids: List[String])(implicit client: KeystoneClient[F]): F[Unit] = ids.traverse_(client.groups.removeUser(self.id, _))
  def isUserInGroup[F[_]: Sync](id: String)(implicit client: KeystoneClient[F]): F[Boolean] = client.groups.isUserInGroup(self.id, id)
}