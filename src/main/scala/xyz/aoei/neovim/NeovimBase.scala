package xyz.aoei.neovim

import java.io.{InputStream, OutputStream}

import xyz.aoei.msgpack.rpc.{ExtendedType, Session}

class NeovimBase(in: InputStream, out: OutputStream, types: List[ExtendedType[_ <: AnyRef]] = List()) {
  val session = new Session(in, out, types)
}
