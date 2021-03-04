---
layout: docs
title: Spray JSON
section: "integrations"
---

## Kebs eliminates spray-json induced boilerplate (kebs-spray-json)

Writing JSON formats in spray can be really unwieldy. For every case-class you want serialized, you have to count the number of fields it has.
And if you want a 'flat' format for 1-element case classes, you have to wire it yourself


```scala
def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
new JsonFormat[T] {
  override def read(json: JsValue): T = construct(jr.read(json))
  override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
}
```

All of this can be left to `kebs-spray-json`. Let's pretend we are to write an `akka-http` router:

```scala
class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {
  import ThingProtocol._
  def createRoute = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
    complete {
      thingsService.create(request).map[ToResponseMarshallable] {
        case ThingCreateResponse.Created(thing) => Created  -> thing
        case ThingCreateResponse.AlreadyExists  => Conflict -> Error("Already exists")
      }
    }
  }
}
```

The source of boilerplate is `ThingProtocol` which can grow really big

```scala
 trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val urlJsonFormat = new JsonFormat[URL] {
      override def read(json: JsValue): URL = json match {
        case JsString(url) => Try(new URL(url)).getOrElse(deserializationError("Invalid URL format"))
        case _             => deserializationError("URL should be string")
      }

      override def write(obj: URL): JsValue = JsString(obj.toString)
    }

    implicit val uuidFormat = new JsonFormat[UUID] {
      override def write(obj: UUID): JsValue = JsString(obj.toString)

      override def read(json: JsValue): UUID = json match {
        case JsString(uuid) => Try(UUID.fromString(uuid)).getOrElse(deserializationError("Expected UUID format"))
        case _              => deserializationError("Expected UUID format")
      }
    }
  }

object ThingProtocol extends JsonProtocol {
  def jsonFlatFormat[P, T <: Product](construct: P => T)(implicit jw: JsonWriter[P], jr: JsonReader[P]): JsonFormat[T] =
    new JsonFormat[T] {
      override def read(json: JsValue): T = construct(jr.read(json))
      override def write(obj: T): JsValue = jw.write(obj.productElement(0).asInstanceOf[P])
    }

  implicit val errorJsonFormat              = jsonFormat1(Error.apply)
  implicit val thingIdJsonFormat            = jsonFlatFormat(ThingId.apply)
  implicit val tagIdJsonFormat              = jsonFlatFormat(TagId.apply)
  implicit val thingNameJsonFormat          = jsonFlatFormat(ThingName.apply)
  implicit val thingDescriptionJsonFormat   = jsonFlatFormat(ThingDescription.apply)
  implicit val locationJsonFormat           = jsonFormat2(Location.apply)
  implicit val createThingRequestJsonFormat = jsonFormat5(ThingCreateRequest.apply)
  implicit val thingJsonFormat              = jsonFormat6(Thing.apply)
}
```

But all of this can be generated automatically, can't it? You only need to import `KebsSpray` trait and you're done:

```scala
object ThingProtocol extends JsonProtocol with KebsSpray
```

Additionally, `kebs-spray-json` tries hard to be smart. It prefers 'flat' format when it comes across 1-element case-classes
In case like this:

```scala
case class ThingId(uuid: UUID)
case class ThingName(name: String)

case class Thing(id: ThingId, name: ThingName, ...)
```

it'll do what you probably expected - `{"id": "uuid", "name": "str"}`. But it also takes into account
if you want `RootJsonFormat` or not. So `case class Error(message: String)` in `Conflict -> Error("Already exists")` will be formatted as
`{"message": "Already exists"}` in JSON.

What if you do not want to use 'flat' format by default?
You have three options to choose from:
* redefine implicits for case-classes you want serialized 'non-flat'
```scala
case class Book(name: String, chapters: List[Chapter])
case class Chapter(name: String)

implicit val chapterRootFormat: RootJsonFormat[Chapter] = jsonFormatN[Chapter]

test("work with nested single field objects") {
    val json =
      """
        | {
        |   "name": "Functional Programming in Scala",
        |   "chapters": [{"name":"first"}, {"name":"second"}]
        | }
      """.stripMargin
    
    json.parseJson.convertTo[Book] shouldBe Book(
      name = "Functional Programming in Scala",
      chapters = List(Chapter("first"), Chapter("second"))
    )
}
```

* mix-in `KebsSpray.NonFlat` if you want _flat_ format to become globally turned off for a protocol
```scala
object KebsProtocol extends DefaultJsonProtocol with KebsSpray.NoFlat
```

* use `noflat` annotation on selected case-classes (thanks to @dbronecki)
```scala
case class Book(name: String, chapters: List[Chapter])
@noflat case class Chapter(name: String)
```


Often you have to deal with convention to have **`snake-case` fields in JSON**.
That's something `kebs-spray-json` can do for you as well

```scala
object ThingProtocol extends JsonProtocol with KebsSpray.Snakified
```

Another advantage is that _snakified_ names are computed during computation, so in run-time they're just string constants.

`kebs-spray-json` also can deal with `enumeratum` enums.

```scala
object ThingProtocol extends JsonProtocol with KebsSpray with KebsEnumFormats
```

As in slick's example, you have two additional enum serialization strategies:
_uppercase_ i _lowercase_ (`KebsEnumFormats.Uppercase`, `KebsEnumFormats.Lowercase`), as well as support for `ValueEnumEntry`

It can also generate recursive formats via `jsonFormatRec` macro, as in the following example:

```scala
case class Thing(thingId: String, parent: Option[Thing])
implicit val thingFormat: RootJsonFormat[Thing] = jsonFormatRec[Thing]
```

`kebs-spray-json` also provides JSON formats for case classes with more than 22 fields.
