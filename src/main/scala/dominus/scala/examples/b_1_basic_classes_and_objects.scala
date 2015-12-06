package dominus.scala.examples


/**
 * [CH4] Classes and Objects
 *
 */
object a_classes_and_objects {

  def f(): Unit = "this String gets lost"

  //f: ()Unit
  def g() {
    "this String gets lost too"
  }

  //g: ()Unit  //NOTE: return type is Unit if without =
  def h() = {
    "this String gets returned!"
  } //h: ()java.lang.String

}

class ChecksumAccumulator {
  private var sum = 0

  //NOTE: method parameters are vals not vars
  // One important characteristic of method parameters in Scala is that they are vals, not vars
  def add(b: Byte): Unit = {
    sum += b
    //    b = 3  error!
  }

  //NOTE: implicit return
  //In the absence of any explicit return statement, a Scala method returns the last value computed by the method.
  def checksum(): Int = {
    ~(sum & 0xFF) + 1
  }
}

class ChecksumAccumulator_2 {
  private var sum = 0

  //NOTE: method shorthand
  // Another shorthand for methods is that you can leave off the curly braces if a method computes only a single result expression.
  //If the result expression is short, it can even be placed on the same line as the def itself.
  def add(b: Byte): Unit = sum += b

  //NOTE: procedure-style method
  //a method that is executed only for its side effects
  def add2(b: Byte) {
    sum += b
  }

  def checksum(): Int = ~(sum & 0xFF) + 1
}

import scala.collection.mutable.Map

//NOTE: Singleton objects / companion object
//You must define both the class and its companion object in the same source file.
//NOTE1: Scala is more object-oriented than Java is that classes in Scala cannot have static members
//NOTE1: companion object is the home for any static methods you might have written in Java
object ChecksumAccumulator {
  private val cache = Map[String, Int]()

  def calculate(s: String): Int =
    if (cache.contains(s))
      cache(s)
    else {
      val acc = new ChecksumAccumulator
      for (c <- s)
        acc.add(c.toByte)
      val cs = acc.checksum()
      cache += (s -> cs)
      cs
    }
}

