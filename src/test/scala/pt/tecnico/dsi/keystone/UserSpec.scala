package pt.tecnico.dsi.keystone

import pt.tecnico.dsi.keystone.models.User

class UserSpec extends Utils {
  "The user service" should {
    "list users" in idempotently { client =>
      for {
        usernames <- client.users.list().map(_.name).compile.toList
      } yield usernames should contain (client.session.user.name)
    }

    "create users" in idempotently { client =>
      for {
        user <- client.users.create(User("teste", domainId = "default"))
        users <- client.users.getByName(user.name).compile.toList
      } yield users should contain (user)
    }

    "get a user" in idempotently { client =>
      for {
        usernames <- client.users.getByName("admin").map(_.name).compile.toList
      } yield usernames should contain ("admin")
    }

    "delete a user" in {
      for {
        client <- scopedClient
        user <- client.users.create(User("teste2", domainId = "default"))
        // This also tests deleting an unexisting user
        _ <- client.users.delete(user.id).idempotently(_ shouldBe ())
        usernames <- client.users.getByName(user.name).map(_.id).compile.toList
      } yield usernames should not contain user.id
    }
  }
}
