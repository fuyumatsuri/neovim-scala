package xyz.aoei.neovim.bindings

object Function {
  // regex to match on array types. Pattern matching on both "ArrayOf(String)" and "ArrayOf(String, 2)" with
  // `ArrayOf(arrayType)` will result in `arrayType = "String"`
  val ArrayOf = """ArrayOf\((.+?)[\,\)].*""".r

  // Map for any type where the provided string doesn't match the scala type
  // Strings are returned so that we don't have to worry about extended types
  val typeMap = Map (
    "Integer" -> "Int",
    "void" -> "Unit",
    "Dictionary" -> "Map[String, Int]", // currently only used for `vim_get_color_map` which is `Map[String, Int]`
    "Array" -> "Array[Any]"
  )

  def getType(t: String): String = t match {
    case ArrayOf(arrayType) => "List[" + getType(arrayType) + "]"
    case _ => typeMap.getOrElse(t, t)
  }
}

class Function(data: Map[String, String], types: List[NvimType]) {
  private def toCamelCase(str: String): String = {
    val s: List[String] = str.split("_").toList
    s.head :: (s.tail map {x => x.head.toUpper + x.tail}) mkString
  }

  val apiName: String = data("name")

  val (nvimType: String, prefix: String) = types find (x => apiName.startsWith(x.prefix)) match {
    case Some(x) => (x.name, x.prefix)
    case None => ("Nvim", "nvim_")
  }

  val name: String = toCamelCase(apiName.split(prefix).last)

  val requestType: String = Function.getType(data("return_type"))

  val returnType: String = requestType match {
    case x if x == "Unit" => x
    case x => "Future[" + x + "]"
  }

  val params: List[List[String]] = data("parameters").asInstanceOf[List[List[String]]]

  val deprecated: Int = data.getOrElse("deprecated_since", -1).asInstanceOf[Int]
}
