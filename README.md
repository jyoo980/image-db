# image-db ![Scala CI](https://github.com/jyoo980/image-db/workflows/Scala%20CI/badge.svg?branch=master) #

A very simple image repository service, written in Scala.

## Requirements ##

* [Scala 2.13+](https://www.scala-lang.org/)
* [JDK 11+](https://www.oracle.com/ca-en/java/technologies/javase-jdk11-downloads.html)
* [sbt (Simple build tool)](https://www.scala-sbt.org/)
* [MongoDB Community Edition](https://www.mongodb.com/)

## Routes ##

`GET /`
* Returns a list of all added images.

`GET /fun`
* Returns the splash page of this application (for fun)

`GET /images/author/:author`
* Returns a list of all added images associated with the given author
* Usage: `curl localhost:8080/images/:author`

`GET /images/metadata/author/:author`
* Returns a list of metadata for the images associated with the given author
* Usage: `curl localhost:8080/images/metadata/author/:author`

Sample response
```sh
$ curl localhost:8080/images/metadata/author/yoo
{
  "results" : [
    {
      "name" : "tenet.jpg",
      "author" : "yoo",
      "size" : 156909,
      "ext" : "jpeg",
      "location" : "./tenet.jpg"
    },
    {
      "name" : "meme.jpg",
      "author" : "yoo",
      "size" : 89607,
      "ext" : "jpeg",
      "location" : "./meme.jpg"
    },
    {
      "name" : "mystery-image.gif",
      "author" : "yoo",
      "size" : 90945,
      "ext" : "gif",
      "location" : "./mystery-image.gif"
    }
  ]
}
```

`GET /images/metadata/:id`
* Returns the metadata of an image with the given id
* Usage: `curl localhost:8080/images/metadata/:id`

Sample response
```sh
$ curl localhost:8080/images/metadata/meme.jpg
{
  "results" : {
    "name" : "tenet.jpg",
    "author" : "yoo",
    "size" : 156909,
    "ext" : "jpeg",
    "location" : "./tenet.jpg"
  }
}
```

`POST /images/:author/:id`
* Adds an image with the given author and id (usually filename) to the server.
* Usage: `curl -F "image=@<path_to_image>" localhost:8080/images/:author/:id`

`DELETE /images/:author/:id`
* Deletes the image with the given author and id from the server.
* Usage: `curl -X DELETE localhost:8080/images/:author/:id`

`DELETE /images/author/:author`
* Deletes the images associated with the given author from the server.
* Usage: `curl -X DELETE localhost:8080/images/:author`

## Build & Run ##

```sh
$ cd image-db
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## Testing ##

```sh
$ sbt test
```

