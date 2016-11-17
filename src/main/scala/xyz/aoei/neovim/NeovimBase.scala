package xyz.aoei.neovim

import java.io.{InputStream, OutputStream}

import xyz.aoei.msgpack.rpc.{ExtendedType, ResponseHandler, Session}

class NeovimBase(in: InputStream, out: OutputStream) {
  def types: List[ExtendedType[_ <: AnyRef]] = Nil

  val session = new Session(in, out, types)

  def onRequest(callback: (String, List[Any], ResponseHandler) => Unit) = session.onRequest(callback)

  def onNotification(callback: (String, List[Any]) => Unit) = session.onNotification(callback)

  def quit(): Unit = session.notify("nvim_command", "qa!")
}
