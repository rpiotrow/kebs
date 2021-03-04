---
layout: docs
title: Akka HTTP
section: "integrations"
---

## Kebs generates akka-http Unmarshaller (kebs-akka-http)

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
