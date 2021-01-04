# image-db #

A very simple image repository service, written in Scala.

## Requirements ##

* [Scala 2.13+](https://www.scala-lang.org/)
* [sbt (Simple build tool](https://www.scala-sbt.org/)

## Routes ##

`GET /`
* Returns a list of all added images.

`POST /images/:author/:id`
* Adds an image with the given author and id (usually filename) to the server.
* Usage: `curl -F "image=@<path_to_image>" localhost:8080/images/:author/:id`

`DELETE /images/:id`
* Deletes the image with the given id from the server.
* Usage: `curl -X DELETE localhost:8080/images/:id`

## Build & Run ##

```sh
$ cd image-db
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
