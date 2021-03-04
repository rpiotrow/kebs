---
layout: docs
title: Play JSON
section: "integrations"
---

## Kebs eliminates play-json induced boilerplate (kebs-play-json)

To be honest `play-json` has never been a source of extensive boilerplate for me- thanks to `Json.format[CC]` macro.
Only _flat_ formats have had to be written over and over. ~~And there is no support for `enumeratum`~~ (support for `enumeratum` is provided by `enumeratum-play-json` and has been removed from `kebs`).
So if you find yourself writing lots of code similar to:

```scala
def flatFormat[P, T <: Product](construct: P => T)(implicit jf: Format[P]): Format[T] =
  Format[T](jf.map(construct), Writes(a => jf.writes(a.productElement(0).asInstanceOf[P])))

implicit val thingIdJsonFormat          = flatFormat(ThingId.apply)
implicit val tagIdJsonFormat            = flatFormat(TagId.apply)
implicit val thingNameJsonFormat        = flatFormat(ThingName.apply)
implicit val thingDescriptionJsonFormat = flatFormat(ThingDescription.apply)

implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
```

, you can delegate it to `kebs-play-json`

```scala
import pl.iterators.kebs.json._
  
implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
```

(or, trait-style)

```scala
object AfterKebs extends JsonProtocol with KebsPlay {
implicit val errorJsonFormat              = Json.format[Error]
implicit val locationJsonFormat           = Json.format[Location]
implicit val createThingRequestJsonFormat = Json.format[ThingCreateRequest]
implicit val thingJsonFormat              = Json.format[Thing]
}
```
