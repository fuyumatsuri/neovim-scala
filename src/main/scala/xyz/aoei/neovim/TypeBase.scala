package xyz.aoei.neovim

import xyz.aoei.msgpack.rpc.Session

abstract class TypeBase {
  val session: Session
  val data: Array[Byte]

  override def equals(o: Any) = o match {
    case that: TypeBase => this.data.deep == that.data.deep
    case _ => false
  }
}