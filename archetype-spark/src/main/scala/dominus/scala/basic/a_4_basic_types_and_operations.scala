package dominus.scala.basic


/**
 *
 * [CH5] Basic Types and Operations
 * NOTE: Data type package  java.lang(String and others(scala)
 *
 *
 *
 */
object a_4_basic_types_and_operations {


  //NOTE: Basic types(java.lang.String and other types from scala.Int/Short/Long/Char/Float/Double/Boolean
  //NOTE1: Other than String, which resides in package java.lang, all of the types are members of package scala.

  //NOTE: String Literals
  //NOTE1: literal for raw strings
  val str =
    """
      |Hello world
      |are you ok
      |test
    """.stripMargin
  println(str)

  //NOTE: Operators are methods


  //TODO: Type Cast


  def main(args: Array[String]) {

    val p = new a_4_basic_types_and_operations()

  }

}

class a_4_basic_types_and_operations {}
