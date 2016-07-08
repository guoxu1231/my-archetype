package dominus.language.scala

import dominus.framework.junit.DominusJUnit4TestBase
import org.junit.Test
import org.junit.Assert._

class TestHelloWorld extends DominusJUnit4TestBase {

  @Test def verifyEasy(): Unit = {
    val greeting = "Hello World";
    assertTrue(greeting.equalsIgnoreCase("hello world"))
  }

}