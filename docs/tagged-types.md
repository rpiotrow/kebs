---
layout: page
title:  "Tagged types"
section: "tagged-types"
position: 3
---

## Tagged types

Starting with version 1.6.0, kebs contain an implementation of, so-called, `tagged types`. If you want to know what a `tagged type` is, please see eg.
[Introduction to Tagged Types](http://www.vlachjosef.com/tagged-types-introduction/) or [Scalaz tagged types description](http://eed3si9n.com/learning-scalaz/Tagged+type.html).
In general, taggging of a type is a mechanism for distinguishing between various instances of the same type. For instance, you might want to use an `Int` to represent an _user id_ or _purchase id_.
But if you use _just an Int_ the compiler will not protest if you use _purchase id_ integer in place of _user id_ integer and vice versa.
To gain additional type safety you could use 1-element case-class wrappers, or, tagged types. In short, you would create `Int @@ UserId` and `Int @@ PurchaseId` types,
where `@@` is _tag_ operator. Thus, you can distinguish between various usages of `Int` while still retaining all `Int` properties ie. `Int @@ UserId` is still an `Int`, but it is not `Int @@ PurchaseId`.

This representation is very useful at times, but there is some boilerplate involved which kebs strives to eliminate. Let's take a look at examples.
To get only the kebs' implementation of tagged types, please add `kebs-tagged` module to your build. You'll then be able to use tagging:

```scala
import pl.iterators.kebs.tagged._

trait UserId
trait PurchaseId

val userId: Int @@ UserId = 10.taggedWith[UserId] 
val purchaseId: Int @@ PurchaseId = 10.@@[PurchaseId]

val userIds: List[Int @@ UserId] = List(10, 15, 20).@@@[UserId]
val purchaseIds: List[Int @@ PurchaseId] = List(10, 15, 20).taggedWithF[PurchaseId]
```

Additionally, if you want to use tagged types in Slick, just mix-in `pl.iterators.kebs.tagged.slick.SlickSupport` (or `import pl.iterators.kebs.tagged.slick._`).

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tagged.slick.SlickSupport

object SlickTaggedExample extends SlickSupport {
  trait UserIdTag
  type UserId = Long @@ UserIdTag

  trait EmailTag
  type Email = String @@ EmailTag

  trait FirstNameTag
  type FirstName = String @@ FirstNameTag

  trait LastNameTag
  type LastName = String @@ LastNameTag

  final case class User(id: UserId, email: Email, firstName: Option[FirstName], lastName: Option[LastName], isAdmin: Boolean)

  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id: Rep[UserId]                   = column[UserId]("id")
    def email: Rep[Email]                 = column[Email]("email")
    def firstName: Rep[Option[FirstName]] = column[Option[FirstName]]("first_name")
    def lastName: Rep[Option[LastName]]   = column[Option[LastName]]("last_name")
    def isAdmin: Rep[Boolean]             = column[Boolean]("is_admin")

    override def * : ProvenShape[User] =
      (id, email, firstName, lastName, isAdmin) <> (User.tupled, User.unapply)
  }

}
```

More often than not, you want to perform some validation before tagging, or, you just want to have a smart constructor that will return tagged representation
whenever criteria are met. You do not have to write it by hand, you can just use `kebs-tagged-meta` which generates all this code for you using `scalameta`.
You just have to tag an object, or a trait, containing your tagged types with `@tagged` annotation.

```scala
import pl.iterators.kebs.tagged._
import pl.iterators.kebs.tag.meta.tagged

@tagged object Tags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag

  type Name  = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]

  type PositiveInt = Int @@ PositiveIntTag
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero     extends Error

    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
  }
}
```

The annotation will translate your code to something like

```scala
object Tags {
  trait NameTag
  trait IdTag[+A]
  trait PositiveIntTag
  
  type Name = String @@ NameTag
  type Id[A] = Int @@ IdTag[A]
  type PositiveInt = Int @@ PositiveIntTag
  
  object Name {
    def apply(arg: String) = from(arg)
    def from(arg: String) = arg.taggedWith[NameTag]
  }
  object Id {
    def apply[A](arg: Int) = from[A](arg)
    def from[A](arg: Int) = arg.taggedWith[IdTag[A]]
  }
  
  object PositiveInt {
    sealed trait Error
    case object Negative extends Error
    case object Zero extends Error
    def validate(i: Int) = if (i == 0) Left(Zero) else if (i < 0) Left(Negative) else Right(i)
    
    def apply(arg: Int) = from(arg).getOrElse(throw new IllegalArgumentException(arg.toString))
    def from(arg: Int) = validate(arg).right.map(arg1 => arg1.taggedWith[PositiveIntTag])
  }
  
  object PositiveIntTag {
    implicit val PositiveIntCaseClass1Rep = new CaseClass1Rep[PositiveInt, Int](PositiveInt.apply(_), identity)
  }
  object IdTag {
    implicit def IdCaseClass1Rep[A] = new CaseClass1Rep[Id[A], Int](Id.apply(_), identity)
  }
  object NameTag {
    implicit val NameCaseClass1Rep = new CaseClass1Rep[Name, String](Name.apply(_), identity)
  }
}
```

You can use generated `from` and `apply` methods as constructors of tagged type instance.

```scala
trait User

val someone = Name("Someone")
//someone: String @@ Tags.NameTag = Someone

val userId = Id[User](10)
//userId: Int @@ Tags.IdTag[User] = 10

val right = PositiveInt.from(10)
//right: scala.util.Either[Tags.PositiveInt.Error,Int @@ Tags.PositiveIntTag] = Right(10)

val notRight = PositiveInt.from(-10)
//notRight: scala.util.Either[Tags.PositiveInt.Error,Int @@ Tags.PositiveIntTag] = Left(Negative)

val alsoRight = PositiveInt(10)
//alsoRight: Int @@ Tags.PositiveIntTag = 10

PositiveInt(-10)
// java.lang.IllegalArgumentException: -10
```

There are some conventions that are assumed during generation.
* tags have to be empty traits (possibly generic)
* tagged types have to be aliases in form of `type X = SomeType @@ Tag` (possibly generic)
* validation methods for tagged type X have to be defined in `object X` and have to:
    * be public
    * be named `validate`
    * take no type parameters
    * take a single argument
    * return Either (this is not enforced though - you'll have a compilation error later)

Also, `CaseClass1Rep` is generated for each tag meaning you will get a lot of `kebs` machinery for free eg. spray formats etc.
