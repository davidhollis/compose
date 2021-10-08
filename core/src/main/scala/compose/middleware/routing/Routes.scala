package compose.middleware.routing

import compose.Application

object Routes {

  def apply[Body](rules: Rule[Body]*): Application[Body] =
    rules.foldLeft[Rule[Body]](new NoMatchRule)(_ orElse _)

}

/*
class Foo {

  def createRoutes: Unit = {
    Routes(
      post("/greeting")   |-> UpdateDefaultGreeting(),
      get("/greet/me")    |-> DefaultGreeter(),
      get("/greet/:name") |-> Greeter(config),
      mount("/insult")    |-> InsultGenerator(),
      mount("/users")     |-> resources(UserBundle()),
      root                |-> WelcomePage(),
      notFound            |-> NotFoundPage(),
    )
  }

  def resources[Body](appBundle: ResourcefulBundle[Body]): Application[Body] =
    Rules(
      get & root       |-> appBundle.index,
      get("/:id")      |-> appBundle.show,
      get("/create")   |-> appBundle.createForm,
      put & root       |-> appBundle.createAction,
      get("/:id/edit") |-> appBundle.editForm,
      post("/:id")     |-> appBundle.editAction,
      delete("/:id")   |-> appBundle.deleteAction,
    )

}
 */
