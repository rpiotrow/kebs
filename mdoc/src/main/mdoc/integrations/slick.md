---
layout: docs
title: Slick
section: "integrations"
---

## Kebs generates slick mappers for your case-class wrappers (kebs-slick)

If you want to model the following table

```scala

case class UserId(userId: String)             extends AnyVal
case class EmailAddress(emailAddress: String) extends AnyVal
case class FullName(fullName: String)         extends AnyVal

//...

class People(tag: Tag) extends Table[Person](tag, "people") {
  def userId: Rep[UserId]                           = column[UserId]("user_id")
  def emailAddress: Rep[EmailAddress]               = column[EmailAddress]("email_address")
  def fullName: Rep[FullName]                       = column[FullName]("full_name")
  def mobileCountryCode: Rep[String]                = column[String]("mobile_country_code")
  def mobileNumber: Rep[String]                     = column[String]("mobile_number")
  def billingAddressLine1: Rep[AddressLine]         = column[AddressLine]("billing_address_line1")
  def billingAddressLine2: Rep[Option[AddressLine]] = column[Option[AddressLine]]("billing_address_line2")
  def billingPostalCode: Rep[PostalCode]            = column[PostalCode]("billing_postal_code")
  def billingCity: Rep[City]                        = column[City]("billing_city")
  def billingCountry: Rep[Country]                  = column[Country]("billing_country")
  def taxId: Rep[TaxId]                             = column[TaxId]("tax_id")
  def bankName: Rep[BankName]                       = column[BankName]("bank_name")
  def bankAccountNumber: Rep[BankAccountNumber]     = column[BankAccountNumber]("bank_account_number")
  def recipientName: Rep[RecipientName]             = column[RecipientName]("recipient_name")
  def additionalInfo: Rep[AdditionalInfo]           = column[AdditionalInfo]("additional_info")
  def workCity: Rep[City]                           = column[City]("work_city")
  def workArea: Rep[Area]                           = column[Area]("work_area")

  protected def mobile = (mobileCountryCode, mobileNumber) <> (Mobile.tupled, Mobile.unapply)
  protected def billingAddress =
    (billingAddressLine1, billingAddressLine2, billingPostalCode, billingCity, billingCountry) <> (Address.tupled, Address.unapply)
  protected def billingInfo =
    (billingAddress, taxId, bankName, bankAccountNumber, recipientName, additionalInfo) <> (BillingInfo.tupled, BillingInfo.unapply)

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea) <> (Person.tupled, Person.unapply)
}

```

then you are forced to write this:

```scala
object People {
  implicit val userIdColumnType: BaseColumnType[UserId]                 = MappedColumnType.base(_.userId, UserId.apply)
  implicit val emailAddressColumnType: BaseColumnType[EmailAddress]     = MappedColumnType.base(_.emailAddress, EmailAddress.apply)
  implicit val fullNameColumnType: BaseColumnType[FullName]             = MappedColumnType.base(_.fullName, FullName.apply)
  implicit val addressLineColumnType: BaseColumnType[AddressLine]       = MappedColumnType.base(_.line, AddressLine.apply)
  implicit val postalCodeColumnType: BaseColumnType[PostalCode]         = MappedColumnType.base(_.postalCode, PostalCode.apply)
  implicit val cityColumnType: BaseColumnType[City]                     = MappedColumnType.base(_.city, City.apply)
  implicit val areaColumnType: BaseColumnType[Area]                     = MappedColumnType.base(_.area, Area.apply)
  implicit val countryColumnType: BaseColumnType[Country]               = MappedColumnType.base(_.country, Country.apply)
  implicit val taxIdColumnType: BaseColumnType[TaxId]                   = MappedColumnType.base(_.taxId, TaxId.apply)
  implicit val bankNameColumnType: BaseColumnType[BankName]             = MappedColumnType.base(_.name, BankName.apply)
  implicit val recipientNameColumnType: BaseColumnType[RecipientName]   = MappedColumnType.base(_.name, RecipientName.apply)
  implicit val additionalInfoColumnType: BaseColumnType[AdditionalInfo] = MappedColumnType.base(_.content, AdditionalInfo.apply)
  implicit val bankAccountNumberColumnType: BaseColumnType[BankAccountNumber] =
    MappedColumnType.base(_.number, BankAccountNumber.apply)
}
    
```

**`kebs` can do it automagically for you**

```scala

import pl.iterators.kebs._

class People(tag: Tag) extends Table[Person](tag, "people") {
  def userId: Rep[UserId]                           = column[UserId]("user_id")
  //...
}

```

If you prefer to **mix in trait** instead of import (for example you're using a custom driver like `slick-pg`), you can do it as well:

```scala
import pl.iterators.kebs.Kebs
object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with Kebs
}

import MyPostgresProfile.api._
```

**`kebs-slick` can also generate mappings for Postgres ARRAY type**, which is a common source of boilerplate in `slick-pg`.
Instead of:

```scala

object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits {
    implicit val institutionListTypeWrapper =
      new SimpleArrayJdbcType[Long]("int8").mapTo[Institution](Institution, _.value).to(_.toList)
    implicit val marketFinancialProductWrapper =
      new SimpleArrayJdbcType[String]("text").mapTo[MarketFinancialProduct](MarketFinancialProduct, _.value).to(_.toList)
  }
}

import MyPostgresProfile.api._

class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

  def * = (id, institutions, mktFinancialProducts)
}

```

you can do just:

```scala

object MyPostgresProfile extends ExPostgresDriver with PgArraySupport {
  override val api: API = new API {}
  trait API extends super.API with ArrayImplicits with Kebs
}

import MyPostgresProfile.api._
class ArrayTestTable(tag: Tag) extends Table[(Long, List[Institution], Option[List[MarketFinancialProduct]])](tag, "ArrayTest") {
  def id                   = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def institutions         = column[List[Institution]]("institutions")
  def mktFinancialProducts = column[Option[List[MarketFinancialProduct]]]("mktFinancialProducts")

  def * = (id, institutions, mktFinancialProducts)
}

```

**`kebs` also supports `Enumeratum`**
Let's go back to the previous example. If you wanted to add a column of type `EnumEntry`, then you would have to write mapping for it:

```scala

sealed trait WorkerAccountStatus extends EnumEntry
object WorkerAccountStatus extends Enum[WorkerAccountStatus] {
  case object Unapproved extends WorkerAccountStatus
  case object Active     extends WorkerAccountStatus
  case object Blocked    extends WorkerAccountStatus

  override val values = findValues
}

object People {

  //...
  
  implicit val workerAccountStatusColumnType: BaseColumnType[WorkerAccountStatus] =
    MappedColumnType.base(_.entryName, WorkerAccountStatus.withName)
}

class People(tag: Tag) extends Table[Person](tag, "people") {
  import People._

  //...
  
  def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

  //...

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
}

```

`kebs` takes care of this as well:

```scala

import pl.iterators.kebs._
import enums._

class People(tag: Tag) extends Table[Person](tag, "people") {

  //...

  def status: Rep[WorkerAccountStatus]              = column[WorkerAccountStatus]("status")

  //...

  override def * : ProvenShape[Person] =
    (userId, emailAddress, fullName, mobile, billingInfo, workCity, workArea, status) <> (Person.tupled, Person.unapply)
  }

```

You can also choose between a few **strategies of writing enums**.
If you just import `enums._`, then you'll get its `entryName` in db.
If you import `enums.lowercase._` or `enums.uppercase._` then it'll save enum name in, respectively, lower or upper case
Of course, enums also work with traits:

```scala
import pl.iterators.kebs.Kebs
import pl.iterators.kebs.enums.KebsEnums

object MyPostgresProfile extends ExPostgresDriver {
  override val api: API = new API {}
  trait API extends super.API with Kebs with KebsEnums.Lowercase /* or KebsEnums, KebsEnums.Uppercase etc. */
}

import MyPostgresProfile.api._
```

`kebs` also supports `ValueEnum`s, to save something other than entry's name to db. For example, if you wanted `WorkerAccountStatus` to be saved as int value, you'd write:

 ```scala
 sealed abstract class WorkerAccountStatusInt(val value: Int) extends IntEnumEntry
 object WorkerAccountStatusInt extends IntEnum[WorkerAccountStatusInt] {
     case object Unapproved extends WorkerAccountStatusInt(0)
     case object Active     extends WorkerAccountStatusInt(1)
     case object Blocked    extends WorkerAccountStatusInt(2)
    
     override val values = findValues
 }
 ```
