---
layout: docs
title: JsonSchema
section: "integrations"
---

## JsonSchema support

Kebs contains a macro, which generate wrapped jsonschema object of `https://github.com/andyglow/scala-jsonschema`.
Kebs also provides proper implicits conversions for their tagged types and common Java types.
To get your json schema you need to use import pl.iterators.kebs.jsonschema.KebsJsonSchema
(together with pl.iterators.kebs.jsonschema.KebsJsonSchemaPredefs if you need support for more Java types).

```scala
import com.github.andyglow.json.JsonFormatter
import com.github.andyglow.jsonschema.AsValue
import json.schema.Version.Draft07
import pl.iterators.kebs.jsonschema.{KebsJsonSchema, JsonSchemaWrapper}

case class WrappedInt(int: Int)
case class WrappedIntAnyVal(int: Int) extends AnyVal
case class Sample(someNumber: Int,
                  someText: String,
                  arrayOfNumbers: List[Int],
                  wrappedNumber: WrappedInt,
                  wrappedNumberAnyVal: WrappedIntAnyVal)

object Sample extends KebsJsonSchema {

  object SchemaPrinter {
    def printWrapper[T](id: String = "id")(implicit schemaWrapper: JsonSchemaWrapper[T]): String =
      JsonFormatter.format(AsValue.schema(schemaWrapper.schema, Draft07(id)))
  }

  SchemaPrinter.printWrapper[Sample]()

}

```
