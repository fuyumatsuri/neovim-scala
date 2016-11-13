package xyz.aoei.neovim

import xyz.aoei.msgpack.rpc.Session

abstract class TypeBase {
  val session: Session
  val data: Array[Byte]
}
