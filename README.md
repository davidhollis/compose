# compose

compose is a functional web application framework written in Scala. The aim is to eventually provide a full-featured backend web stack that encourages building applications through defining and composing small functions.


## Why?

Did the world need another web framework? No, not really.

I started work on this because I was interested in seeing how far I could go in building a web frameowrk based on three principles:

  1. A web application is just a function of type `Request => Response`
  2. A web server is just a function of type `Application => Nothing` (i.e., a function that takes an application and does not return)
  3. Middleware is any kind of function that composes to modify the behavior of a web application (e.g., compressing response bodies or authenticating requests), or to produce one from a combination of others (e.g., routing).


## Status

At the moment, compose is extremely incomplete. There's a development server that can respond to requests, but no attempt has been made to ensure that it's correct or reliable enough for production use. It's definitely not ready for production, and it may never be.


## Getting Started

First, ensure that you have:

  - a jdk compatible with at least Java 9 (compose makes use of some IO methods not present in jdk8)
  - Scala 2.13
  - sbt 1.3

Then you can start the demo application with:

```bash
sbt 'runMain compose.demos.GreetingsDemo'
```

From another terminal, you can hit the demo server:

```bash
curl -v 'http://127.0.0.1:8080/greet/World'
```

### Developing in compose

The bulk of the code and tests lives in the `core` project directory. You can use all of the normal sbt commands to compile (`sbt compile`), run tests (`sbt test`), or open a scala repl with everything loaded (`sbt console`).

Documentation is ... a work in progress, but the basic types ([`types.scala`](core/src/main/scala/compose/types.scala)) and the [`Request`](core/src/main/scala/compose/http/Request.scala) and [`Response`](core/src/main/scala/compose/http/Response.scala) classes should be good entry points to understanding how everything fits together.

## Contributing

You can find contributing guidelines [here](CONTRIBUTING.md) and the Code of Conduct [here](CODE_OF_CONDUCT.md).

 * * *

 &copy; 2020 David Hollis