/**
 *
 * Scala-styl boiler-plate code
 * 1, Constructor parameters become public “fields”
 * (Scala-style, which means that they really just have an associated accessor/mutator method pair)
 *
 * 2, Methods toString(), equals() and hashCode() are defined based on the constructor fields
 *
 * 3, A companion object containing: An apply() constructor based on the class constructor
      An extractor based on constructor fields
 *
 */
//EE: case class freeing us from the boiler-plate tyranny of endless getter/setter declarations and the manual labor of proper equals() and toString() methods

case class Person1(firstName: String, lastName: String)

//By prefixing each constructor field with the var keyword, we are effectively instructing the compiler
// to generate a mutator as well as an accessor method.
case class Person2(var firstName: String, var lastName: String)



val me = Person1("Daniel", "Spiewak")
val first = me.firstName
val last = me.lastName
//EE: Considering how many times I have built “Java Bean” classes solely for the purpose of wrapping data up in a nice neat package,
// it is easy to see where this sort of syntax sugar could be useful.

