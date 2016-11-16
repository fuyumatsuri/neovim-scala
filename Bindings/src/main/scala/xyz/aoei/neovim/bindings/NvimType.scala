package xyz.aoei.neovim.bindings

class NvimType(val name: String, data: Map[String, String]) {
  val id: Int = data("id").asInstanceOf[Int]
  val prefix: String = data("prefix")
}
