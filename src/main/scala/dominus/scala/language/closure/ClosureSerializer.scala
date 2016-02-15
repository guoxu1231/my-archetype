package dominus.scala.language.closure

import java.io._
import java.nio.file.{Files, Paths}

import scala.reflect.ClassTag
import scala.reflect.io.File


/**
 * Scala closures are Java objects and can be serialized using Java serialization;
   this is a feature of Scala that makes it relatively straightforward to send a computation to another machine

   Apache Spark Class shipping: To let the worker nodes fetch the bytecode for the classes created on each line, we
   made the interpreter serve these classes over HTTP
 */
object ClosureSerializer {
  //NOTE: "spark.closure.serializer", "org.apache.spark.serializer.JavaSerializer"
  def serialize[T: ClassTag](t: T): Array[Byte] = {

    val bos = new ByteArrayOutputStream()
    val objOut = new ObjectOutputStream(bos)
    objOut.writeObject(t)
    objOut.close()
    bos.toByteArray
  }

  def deserialize[T: ClassTag](bytes: Array[Byte]): T = {
    //    val bis = new ByteBufferInputStream(bytes)
    //    val in = deserializeStream(bis)
    //    in.readObject()

    val objIn = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
      override def resolveClass(desc: ObjectStreamClass): Class[_] = {
        // scalastyle:off classforname
        Class.forName(desc.getName, true, Thread.currentThread.getContextClassLoader)
        // scalastyle:on classforname
      }
    }
    objIn.readObject().asInstanceOf[T]
  }


  def main(args: Array[String]) {


    File("/tmp/closure.output").writeBytes(ClosureSerializer.serialize((x: Int) => {
      println("amazing serializable!")
      x + 1
    }))

    //NOTE: "we can  construct an object only if the class is available"
    //NOTE: Exception in thread "main" java.lang.ClassNotFoundException: dominus.scala.language.ClosureSerializer$$anonfun$main$1
    println(ClosureSerializer.deserialize(Files.readAllBytes(Paths.get("/tmp/closure.output"))).asInstanceOf[Int=>Int](5))


  }
}




