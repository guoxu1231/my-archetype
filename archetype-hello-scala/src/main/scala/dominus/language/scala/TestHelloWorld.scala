package dominus.language.scala

import org.junit.Assert._
import org.junit.Test

class TestHelloWorld {

  @Test def verifyEasy(): Unit = {
    val greeting = "Hello World";
    assertTrue(greeting.equalsIgnoreCase("hello world"))
  }

}