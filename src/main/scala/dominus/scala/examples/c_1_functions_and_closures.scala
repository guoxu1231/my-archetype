package dominus.scala.examples


/**
 * [CH8] Functions and Closures
 */
object c_1_functions_and_closures {


  //NOTE: local function
  //define functions inside other functions. Just like local variables


  //NOTE: function literal / first-class functions
  //NOTE1: The syntax of a function literal in Scala,  (x: Int, y: Int) => x + y
  //The => designates that this function converts the thing on the left (any integer x) to the thing on the right (x + 1)
  var increase = (x: Int) => x + 1
  println(increase)

  //NOTE1: multiple statement function literal
  increase = (x: Int) => {
    println("We")
    println("are")
    println("here!")
    x + 1
  }

  //TODO function value, magic?
  //Every function value is an instance of some class that extends one of several FunctionN traits in package scala

  //NOTE: Short forms of function literals
  val someNumbers = List(-11, -10, -5, 0, 5, 10)
  someNumbers.filter((x: Int) => x > 0)
  //NOTE1: function parameter type, type inference from the target type
  //The Scala compiler knows that x must be an integer, because it sees that you are immediately using the function to filter a list of integers
  someNumbers.filter((x) => x > 0)
  //NOTE1: leave out parentheses around a parameter whose type is inferred
  someNumbers.filter(x => x > 0)


  //NOTE: Placeholder syntax
  //This Placeholder will be filled in with an argument to the function each time the function is invoked.
  someNumbers.filter(_ > 0)
  //NOTE1: use underscores as placeholders for one or more parameters, so long as each parameter appears only one time within the function literal
  val f = (_: Int) + (_: Int)

  //NOTE: Partially applied functions
  //NOTE1: (extreme simplify function literal, placeholder syntax ++)
  //Leave a space between the function name and the underscore
  //_ is the placeholder for entire parameter list
  someNumbers.foreach(println _)

  def sum(a: Int, b: Int, c: Int) = a + b + c

  val a = sum _
  val b = sum(1, _: Int, 3)

  //NOTE: Closures
  //free variable and bound variable
  //NOTE1:The function value (the object) that’s created at runtime from this function literal is called a closure.
  // The name arises from the act of “closing” the function literal by “capturing” the bindings of its free variables.
  //free variable
  var more = 1
  val addMore = (x: Int) => x + more
  var sum = 0
  someNumbers.foreach(sum += _)
  //NOTE1: Scala’s closures capture variables themselves, not the value to which variables refer.
  println("sum:" + sum)

  //NOTE:Special function call forms
  //TODO

  def main(args: Array[String]) {

  }

}
