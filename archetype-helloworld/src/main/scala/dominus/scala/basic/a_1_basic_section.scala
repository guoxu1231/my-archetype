package dominus.scala.basic


/**
 * [CH2] First Steps in Scala
 * [CH3] Next Steps in Scala
 *
 * Type Inference
 * Scala Operator +-/, ()=apply() []=Parameterize
 *
 * TODO mutable and immutable collection   Array vs List
 *
 */
object a_1_basic_section {

  //NOTE: type inference
  val msg0 = "hello world"
  val numNames = Array("zero", "one", "two")
  val numNames2 = Array.apply("zero", "one", "two")

  //NOTE: type declare
  //In contrast to Java, where you specify a variable’s type before its name, in Scala you specify a variable’s type after its name, separated by a colon.
  var msg1: java.lang.String = "Hello world"

  //NOTE: function declare
  /**
   * A type annotation must follow every function parameter, preceded by a colon, because the Scala compiler does not infer function parameter types
   * @param x
   * @param y
   * @return  [Result Type][Optional],  A method’s result type is the type of the value that results from  calling the method. (In Java, this concept is called the return type.)
   *          [=] The equals sign that precedes the body of a function hints that in the functional world view, a function defines an expression that results in a value.
   */
  def max(x: Int, y: Int): Int = {
    if (x > y) x
    else y
  }

  //NOTE: function literal
  val args = List("one, two, three")
  args.foreach(arg => println(arg))
  args.foreach((arg: String) => println(arg))
  args.foreach(println)

  //NOTE: result type inference
  def max2(x: Int, y: Int) = if (x > y) x else y

  //NOTE: Scala’s Unit type is similar to Java’s void type
  def greet(): Unit = println("Hello, world! from greet")


  //NOTE: Parameterize arrays with types
  val greetStrings = new Array[String](3)
  //NOTE: greetStrings(0) = greetStrings.apply(0)
  // accessing an element of an array in Scala is simply a method call like any other.
  greetStrings(0) = "Hello"
  greetStrings(1) = ", "
  greetStrings(2) = "world!\n"
  println("greetStrings(0) = " + greetStrings.apply(0))

  //NOTE: Operator overloading
  //characters such as +, , *, and / can be used in method names.
  println(1 + 2)
  println((1).+(2))
  val i: Integer = 3
  (i).+(3)

  //NOTE: mutable:Array immutable: List Tuple

  //Like lists, tuples are immutable, but unlike lists, tuples can contain different types of elements
  val pair = (99, "nine nine") //Tuple2[Int, String]
  println(pair._1) //element access
  println(pair._2)
  val tupleSix = ('u', 'r', "the", 1, 4, "me") //Tuple6[Char, Char, String, Int, Int, String]


  def main(args: Array[String]) {

    //    b = new Basic()
    println(max(1, 4))
    println(greet())


  }

}