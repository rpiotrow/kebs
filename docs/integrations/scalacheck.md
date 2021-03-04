---
layout: docs
title: ScalaCheck
section: "integrations"
---

## Scalacheck support

Kebs provides support to use tagged types in your Arbitrary from scalacheck.
Along with this kebs provides support for Java types.
Kebs also introduces term of minimal and maximal generator.
The minimal generator is an generator which always generates empty collection of Option, Set, Map etc.
The maximum - in the opposite - always generate non-empty collections.
Kebs provides an useful trait called AllGenerators which binds minimal, normal and maximal generator all together,
so you can get easy generate the representation you currently need for tests.

```scala
import pl.iterators.kebs.scalacheck.{KebsArbitraryPredefs, KebsScalacheckGenerators}
import java.net.{URI, URL}
import java.time.{Duration, Instant, LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

case class WrappedInt(int: Int)
case class WrappedIntAnyVal(int: Int) extends AnyVal
case class BasicSample(
    someNumber: Int,
    someText: String,
    wrappedNumber: WrappedInt,
    wrappedNumberAnyVal: WrappedIntAnyVal,
)

case class CollectionsSample(
    listOfNumbers: List[Int],
    arrayOfNumbers: Array[Int],
    setOfNumbers: Set[Int],
    vectorOfNumbers: Vector[Int],
    optionOfNumber: Option[Int],
    mapOfNumberString: Map[Int, String],
)

case class JavaTypesSample(
    instant: Instant,
    zonedDateTime: ZonedDateTime,
    localDateTime: LocalDateTime,
    localDate: LocalDate,
    localTime: LocalTime,
    duration: Duration,
    url: URL,
    uri: URI
)

object Sample extends KebsScalacheckGenerators with KebsArbitraryPredefs {

    val basic = allGenerators[BasicSample].normal.generate

    val minimalCollections = allGenerators[CollectionsSample].minimal.generate
    val maximalCollections = allGenerators[CollectionsSample].maximal.generate

    val javaTypes =  allGenerators[JavaTypesSample].normal.generate

}

```
