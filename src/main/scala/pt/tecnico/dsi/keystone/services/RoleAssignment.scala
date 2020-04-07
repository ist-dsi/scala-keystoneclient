package pt.tecnico.dsi.keystone.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.Status.{NotFound, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.keystone.models.{Role, WithId}

trait RoleAssignment[F[_]] { this: BaseService[F] =>
  object roles {
    /**
      * Role assignments for users on this context.
      */
    val users = new RoleAssignmentService(uri, "users", authToken)
    /**
      * Role assignments for groups on this context.
      */
    val groups = new RoleAssignmentService(uri, "groups", authToken)
  }
}

class RoleAssignmentService[F[_]: Sync: Client](val uri: Uri, target: String, authToken: Header) extends BaseService[F](authToken) {
  import dsl._

  /**
    * Lists role assignments for a target on a certain context
    * @param id the object's id
    * @param targetId the target's id
    * @return roles assigned
    */
  def list(id: String, targetId: String): Stream[F, WithId[Role]] =
    genericList[WithId[Role]]("roles", uri / id / target / targetId / "roles")

  /**
    * Assigns a role to a target on a certain context
    * @param id the contexts's id
    * @param targetId the target's id (group/project)
    */
  def assign(id: String, targetId: String, roleId: String): F[Unit] =
    client.fetch(PUT(uri / id / target / targetId / "roles" / roleId, authToken)) {
      case Successful(_) => F.pure(())
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  /**
    * Checks if a certain role is assigned to a target on a context
    * @param id the context's id
    * @param targetId the target's id
    * @return whether the role is assigned
    */
  def check(id: String, targetId: String, roleId: String): F[Boolean] =
    client.fetch(HEAD(uri / id / target / targetId / "roles" / roleId, authToken)) {
      case Successful(_) => F.pure(true)
      case NotFound(_) => F.pure(false)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  /**
    * Unassign role from group on domain
    * @param id the context's id
    * @param targetId the target's id
    */
  def delete(id: String, targetId: String, roleId: String): F[Unit] =
    genericDelete(uri / id / target / targetId / "roles" / roleId)
}