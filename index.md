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
