package dominus.language

import org.junit.Test
import origin.common.junit.DominusJUnit4TestBase
import org.junit.Assert.*

open class TestKotlinHelloWorld : DominusJUnit4TestBase() {
    override fun doSetUp() {
        super.doSetUp()
        println("HelloWorld.doSetup")
    }

    override fun doTearDown() {
        super.doTearDown()
        println("HelloWorld.doTearDown")
    }

    @Test
    fun testHelloWorld() {
        assertTrue(true)
    }

}