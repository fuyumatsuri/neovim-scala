import java.io.{File, InputStream, OutputStream, PrintWriter}

import xyz.aoei.bindings.Generator
import xyz.aoei.msgpack.rpc.Session

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.SyncVar
import scala.sys.process.{Process, ProcessIO}

object Main extends App {
  val inputStream = new SyncVar[InputStream]
  val outputStream = new SyncVar[OutputStream]

  val pb = Process(Seq("nvim", "-u", "NONE", "-N", "--embed"))
  val pio = new ProcessIO(
    stdout => outputStream.put(stdout),
    stdin => inputStream.put(stdin),
    _ => ())
  val process = pb.run(pio)

  val session = new Session(inputStream.get, outputStream.get)
  session.request("vim_get_api_info").onSuccess {
    case result =>
      val fileName = "./Bindings.scala"

      println("Writing bindings to " + fileName)

      val pw = new PrintWriter(new File(fileName))
      pw.write(Generator.generateClassStrings(result).mkString)
      pw.close()

      process.destroy
  }
}
