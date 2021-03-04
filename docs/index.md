---
layout: home
title:  "Home"
section: "home"
position: 1
technologies:
- first:  ["Scala", "sbt-microsites plugin is completely written in Scala"]
- second: ["SBT", "sbt-microsites plugin uses SBT and other sbt plugins to generate microsites easily"]
- third:  ["Jekyll", "Jekyll allows for the transformation of plain text into static websites and blogs."]
---

# Kebs

## Scala library to eliminate boilerplate
[![Maven Central](https://img.shields.io/maven-central/v/pl.iterators/kebs-slick_2.13.svg)]()
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/theiterators/kebs/master/COPYING)
[![Build Status](https://travis-ci.org/theiterators/kebs.svg?branch=master)](https://travis-ci.org/theiterators/kebs)

![logo](https://raw.githubusercontent.com/theiterators/kebs/master/logo.png)

A library maintained by [Iterators](https://www.iteratorshq.com).

## Why?

`kebs` is for eliminating some common sources of Scala boilerplate code that arise when you use
Slick (`kebs-slick`), Spray (`kebs-spray-json`), Play (`kebs-play-json`), Circe (`kebs-circe`), Akka HTTP (`kebs-akka-http`).

## SBT

Support for `slick`

`libraryDependencies += "pl.iterators" %% "kebs-slick" % "1.9.1"`

Support for `spray-json`

`libraryDependencies += "pl.iterators" %% "kebs-spray-json" % "1.9.1"`

Support for `play-json`

`libraryDependencies += "pl.iterators" %% "kebs-play-json" % "1.9.1"`

Support for `circe`

`libraryDependencies += "pl.iterators" %% "kebs-circe" % "1.9.1"`

Support for `json-schema`

`libraryDependencies += "pl.iterators" %% "kebs-jsonschema" % "1.9.1"`

Support for `scalacheck`

`libraryDependencies += "pl.iterators" %% "kebs-scalacheck" % "1.9.1"`

Support for `akka-http`

`libraryDependencies += "pl.iterators" %% "kebs-akka-http" % "1.9.1"`

Support for `tagged types`

`libraryDependencies += "pl.iterators" %% "kebs-tagged" % "1.9.1"`

or for tagged-types code generation support

`libraryDependencies += "pl.iterators" %% "kebs-tagged-meta" % "1.9.1"`
`addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)`

Builds for Scala `2.12` and `2.13` are provided.

## TBD: list of modules (instead of the above sbt enumeration)