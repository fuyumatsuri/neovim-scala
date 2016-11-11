import java.io.{InputStream, OutputStream}

import xyz.aoei.msgpack.rpc.{ExtendedType, Session}

import scala.concurrent.SyncVar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.process.{Process, ProcessIO}
import xyz.aoei.generate.Generator

import treehugger.forest._

object Main extends App {
  val inputStream = new SyncVar[InputStream]
  val outputStream = new SyncVar[OutputStream]

  val pb = Process(Seq("nvim", "-u", "NONE", "-N", "--embed"))
  val pio = new ProcessIO(
    stdout => outputStream.put(stdout),
    stdin => inputStream.put(stdin),
    _ => ())
  pb.run(pio)

  val session = new Session(inputStream.get, outputStream.get)
  session.request("vim_get_api_info").onSuccess {
    case result =>
      val (functions, errorTypes, types) = Generator.parseApiInfo(result)
      println(treeToString(Generator.generateClass(functions, types)))
  }
}
