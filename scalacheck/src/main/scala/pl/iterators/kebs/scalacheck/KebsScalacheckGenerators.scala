package pl.iterators.kebs.scalacheck

import pl.iterators.kebs.scalacheck.macros.KebsScalacheckGeneratorsMacro

trait KebsScalacheckGenerators {
  def allGenerators[T]: pl.iterators.kebs.scalacheck.AllGenerators[T] = macro KebsScalacheckGeneratorsMacro.materializeGenerators[T]
}
