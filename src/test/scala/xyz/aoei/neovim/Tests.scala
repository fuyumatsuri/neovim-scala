package xyz.aoei.neovim

import java.io.{InputStream, OutputStream}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, SyncVar}
import scala.concurrent.duration._
import scala.sys.process.{Process, ProcessIO}

class Tests extends FlatSpec with BeforeAndAfter with ScalaFutures {
  val inputStream = new SyncVar[InputStream]
  val outputStream = new SyncVar[OutputStream]

  try {
    val pb = Process(Seq("nvim", "-u", "NONE", "-N", "--embed"))
    val pio = new ProcessIO(
      stdout => outputStream.put(stdout),
      stdin => inputStream.put(stdin),
      _ => ())
    pb.run(pio)
  } catch {
    case e: Exception =>
      println("A Neovim installation is required to run the tests")
      println("(see https://github.com/neovim/neovim/wiki/Installing)")
      System.exit(1)
  }

  val nvim = new Neovim(inputStream.get, outputStream.get)

  var requests: Array[Any] = Array()
  var notifications: Array[Any] = Array()

  nvim.onRequest((method, args, resp) => {
    requests = requests :+ Array(method, args)
    resp.send("received " + method + "(" + args.toString + ")")
  })

  nvim.onNotification((method, args) => {
    notifications = notifications :+ Array(method, args)
  })

  before {
    requests = Array()
    notifications = Array()
  }

  it should "send requests and receive response" in {
    val f: Future[Any] = nvim.eval("""{"k1": "v1", "k2": "v2"}""")
    ScalaFutures.whenReady(f) { res =>
      assert(res == Map("k1" -> "v1", "k2" -> "v2"))
    }
  }

  it should "deal with custom types" in {
    nvim.command("vsp")
    Await.result(for {
      windows <- nvim.listWins
      window <- {
        assert(windows.length == 2)
        assert(windows.head.isInstanceOf[Window])
        assert(windows(1).isInstanceOf[Window])

        nvim.setCurrentWin(windows(1))
        nvim.getCurrentWin
      }
    } yield assert(window == windows(1)), 1 second)
  }
}
