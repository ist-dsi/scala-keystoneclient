package pt.tecnico.dsi.openstack.keystone

import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.keystone.services._

object KeystoneClient {
  def fromEnvironment[F[_]: Client](env: Map[String, String] = sys.env)(implicit F: Sync[F]): F[KeystoneClient[F]] = {
    F.fromOption(env.get("OS_AUTH_URL"), new Throwable(s"Could not get OS_AUTH_URL from the environment"))
      .flatMap(authUrl => F.fromEither(Uri.fromString(authUrl)))
      .flatMap(baseUri => apply(baseUri).authenticateFromEnvironment(env))
  }

  def apply[F[_]: Client: Sync](baseUri: Uri): UnauthenticatedKeystoneClient[F] = new UnauthenticatedKeystoneClient(baseUri)
}

class KeystoneClient[F[_]: Sync](val baseUri: Uri, val session: Session, val authToken: Header)(implicit client: Client[F]) {
  val uri: Uri = if (baseUri.path.dropEndsWithSlash.toString.endsWith("v3")) baseUri else baseUri / "v3"
  
  val authentication = new Authentication[F](uri, authToken)
  val domains = new Domains[F](uri, authToken)
  val groups = new Groups[F](uri, session, authToken)
  val projects = new Projects[F](uri, session, authToken)
  val regions = new Regions[F](uri, authToken)
  val roles = new Roles[F](uri, authToken)
  val services = new Services[F](uri, authToken)
  val endpoints = new Endpoints[F](uri, authToken)
  val users = new Users[F](uri, session, authToken)
  
  /*
  trait BaseCreate[Create <: BaseCreate[Create]] {
    type Model <: Identifiable
    def service[F[_]](client: KeystoneClient[F]): CrudService[F, Model, Create, _]
  }
  final case class Create(...) extends BaseCreate[Create] {
    override type Model = Domain
    override def service[F[_]](client: KeystoneClient[F]): CrudService[F, Domain, Create, _] = client.domains
  }
  def apply[Create <: BaseCreate[Create]](create: Create, extraHeaders: Header*): F[create.Model] =
    create.service(this).create(create, extraHeaders:_*)
  
  // And a similar approach to update. Ideally we could simplify it even further:
  // https://stackoverflow.com/questions/63994512/type-lambda-with-higher-kind
  */
}