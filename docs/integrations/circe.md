---
layout: docs
title: Circe
section: "integrations"
---

## Kebs eliminates Circe induced boilerplate (kebs-circe)
**Still in experimental stage!**
Circe might be a source of boilerplate depending on the type of derivation you use - if it's semi-auto derivation, you'll
have to write a lot of encoders/decoders for your case classes:
```scala
object BeforeKebs {
    object ThingProtocol extends CirceProtocol with CirceAkkaHttpSupport {
      import io.circe._
      import io.circe.generic.semiauto._
      implicit val thingCreateRequestEncoder: Encoder[ThingCreateRequest] = deriveEncoder
      implicit val thingCreateRequestDecoder: Decoder[ThingCreateRequest] = deriveDecoder
      implicit val thingIdEncoder: Encoder[ThingId]                       = deriveEncoder
      implicit val thingIdDecoder: Decoder[ThingId]                       = deriveDecoder
      implicit val thingNameEncoder: Encoder[ThingName]                   = deriveEncoder
      implicit val thingNameDecoder: Decoder[ThingName]                   = deriveDecoder
      implicit val thingDescriptionEncoder: Encoder[ThingDescription]     = deriveEncoder
      implicit val thingDescriptionDecoder: Decoder[ThingDescription]     = deriveDecoder
      implicit val tagIdEncoder: Encoder[TagId]                           = deriveEncoder
      implicit val tagIdDecoder: Decoder[TagId]                           = deriveDecoder
      implicit val locationEncoder: Encoder[Location]                     = deriveEncoder
      implicit val locationDecoder: Decoder[Location]                     = deriveDecoder
      implicit val thingEncoder: Encoder[Thing]                           = deriveEncoder
      implicit val thingDecoder: Decoder[Thing]                           = deriveDecoder
      implicit val errorMessageDecoder: Decoder[ErrorMessage]             = deriveDecoder
      implicit val errorMessageEncoder: Encoder[ErrorMessage]             = deriveEncoder
    }
    import ThingProtocol._
    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
          }
        }
      }
    }
  }
```

Kebs can get rid of this for you:
```scala
object AfterKebs {
    object ThingProtocol extends KebsCirce with CirceProtocol with CirceAkkaHttpSupport
    import ThingProtocol._

    class ThingRouter(thingsService: ThingsService)(implicit ec: ExecutionContext) {

      def createRoute: Route = (post & pathEndOrSingleSlash & entity(as[ThingCreateRequest])) { request =>
        complete {
          thingsService.create(request).map[ToResponseMarshallable] {
            case ThingCreateResponse.Created(thing) => Created  -> thing
            case ThingCreateResponse.AlreadyExists  => Conflict -> ErrorMessage("Already exists")
          }
        }
      }
    }
  }
```
If you want to disable flat formats, you can mix-in `KebsCirce.NoFlat`:
```scala
object KebsProtocol extends KebsCirce with KebsCirce.NoFlat
```
You can also support snake-case fields in JSON:
```scala
object KebsProtocol extends KebsCirce with KebsCirce.Snakified
```

And capitalized:
```scala
 object KebsProtocol extends KebsCirce with KebsCirce.Capitalized
```

#### - kebs generates akka-http Unmarshaller (kebs-akka-http)

It makes it very easy to use 1-element case-classes or `enumeratum` enums/value enums in eg. `parameters` directive:

```scala
sealed abstract class Column(val value: Int) extends IntEnumEntry
object Column extends IntEnum[Column] {
    case object Name extends Column(1)
    case object Date extends Column(2)
    case object Type extends Column(3)
    
    override val values = findValues
}

sealed trait SortOrder extends EnumEntry
object SortOrder extends Enum[SortOrder] {
    case object Asc  extends SortOrder
    case object Desc extends SortOrder
    
    override val values = findValues
}

case class Offset(value: Int) extends AnyVal
case class Limit(value: Int)  extends AnyVal

case class PaginationQuery(sortBy: Column, sortOrder: SortOrder, offset: Offset, limit: Limit)

import pl.iterators.kebs.unmarshallers._
import enums._

val route = get {
  parameters('sortBy.as[Column], 'order.as[SortOrder] ? (SortOrder.Desc: SortOrder), 'offset.as[Offset] ? Offset(0), 'limit.as[Limit])
    .as(PaginationQuery) { query =>
      //...
    }

}

```
