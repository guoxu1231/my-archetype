package dominus.scala.examples


/**
 * [CH7] Built-in Control Structures
 *
 * 1, One thing you will notice is that almost all of Scala’s control structures result in some value;
 * 2, Imperative languages often have a ternary operator(such as the ?: operator of C, C++, and Java), which behaves exactly like if, but results in a value.
 * Scala then continues this trend by having for, try, and match also result in values.
 */
object a_2_build_in_control_structure {

  //NOTE: Learn to recognize the functional style
  //If the code contains no vars at all—i.e., it contains only vals—it is probably in a functional style
  //One way to move towards a functional style, therefore, is to try to program without vars.

  //NOTE: Built-in Control Structures
  val args = List("one, two, three")

  //NOTE: If expressions
  //  var filename = "default.txt"
  //  if (!args.isEmpty)
  //    filename = args(0)
  //Scala’s if is an expression that results in a value
  //NOTE1: conditional initialization
  val filename = if (!args.isEmpty) args(0) else "default.txt"

  //NOTE: While / do-while loops
  //Scala’s while loop behaves as in other languages

  //NOTE: For expressions
  //NOTE1: Iteration through collections, generator <- & generator expression
  val filesHere = (new java.io.File(".")).listFiles
  for (file <- filesHere)
    println(file)
  for (i <- 1 to 4) //create Ranges using syntax like “1 to 4”
    println("Iteration " + i)
  //[1 to 4] = 1,2,3,4  [1 until 4] = 1,2,3
  for (i <- 1 until 4)
    println("Iteration " + i)
  //NOTE1: Filtering
  for (file <- filesHere if file.getName.endsWith(".scala"))
    println(file)

  //NOTE1: Nested iteration, Mid-stream variable bindings
  //\\
  //NOTE1: Producing a new collection, yield keywords and syntax: for clauses yield body
  def scalaFiles =
    for {file <- filesHere if file.getName.endsWith(".scala")
    } yield file


  //NOTE: Exception handling with try expressions
  // d_scala_java_compare

  //NOTE: Match expressions
  //d_scala_java_compare

  //TODO Living without break and continue

  //TODO Variable scope

  //TODO Refactoring imperative-style code


  def main(args: Array[String]) {

  }
}
