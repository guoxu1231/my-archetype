package dominus.scala.examples


/**
 *
 * [CH6] Functional Objects
 * class parameters
 *
 *
 */
object b_2_class_parameters {


}

//NOTE: Class Parameters & Immutable Rational Object
//defining and using immutable objects is a quite natural way to code in Scala.
//NOTE1: we’ll require that clients provide all data needed by an instance when they construct the instance.
class Rational(n: Int, d: Int) {
  //NOTE1: class parameters  //TODO what's the magic inside class
  //class parameters can be used directly in the body of the class;
  //NOTE1:primary constructor
  //The Scala compiler will gather up these two class parameters and create a primary constructor that takes the same two parameters.


  //NOTE1: checking preconditions, defined in scala.Predef
  require(d != 0)

  private val g = gcd(n.abs, d.abs)
  val numer = n / g
  val denom = d / g


  /**
   * In Scala, every auxiliary constructor must invoke another constructor of
    the same class as its first action. In other words, the first statement in every
    auxiliary constructor in every Scala class will have the form “this(. . . )”.
   */
  //NOTE1: Auxiliary constructors
  def this(n: Int) = this(n, 1)

  def +(that: Rational): Rational =
    new Rational(
      numer * that.denom + that.numer * denom,
      denom * that.denom
    )

  def +(i: Int): Rational =
    new Rational(numer + i * denom, denom)

  def -(that: Rational): Rational =
    new Rational(
      numer * that.denom - that.numer * denom,
      denom * that.denom
    )

  def -(i: Int): Rational =
    new Rational(numer - i * denom, denom)

  def *(that: Rational): Rational =
    new Rational(numer * that.numer, denom * that.denom)

  def *(i: Int): Rational =
    new Rational(numer * i, denom)

  def /(that: Rational): Rational =
    new Rational(numer * that.denom, denom * that.numer)

  def /(i: Int): Rational =
    new Rational(numer, denom * i)

  //NOTE1: reimplementing the toString method
  override def toString = numer + "/" + denom

  //greatest common divisor,  66/42 be normalized to 11/7
  private def gcd(a: Int, b: Int): Int =
    if (b == 0) a else gcd(b, a % b)
}
