package xyz.aoei.generate

object Function {
  // regex to match on array types. Pattern matching on both "ArrayOf(String)" and "ArrayOf(String, 2)" with
  // `ArrayOf(arrayType)` will result in `arrayType = "String"`
  val ArrayOf = """ArrayOf\((.+?)[\,\)].*""".r

  // Map for any type where the provided string doesn't match the scala type
  // Strings are returned so that we don't have to worry about extended types
  val typeMap = Map (
    "Integer" -> "Int",
    "void" -> "Unit"
  )

  def getType(t: String): String = t match {
    case ArrayOf(arrayType) => "List[" + getType(arrayType) + "]"
    case _ => typeMap.getOrElse(t, t)
  }
}

class Function(data: Map[String, String]) {
  private def toCamelCase(s: List[String]): String = s.head :: (s.tail map {x => x.head.toUpper + x.tail}) mkString

  val apiName: String = data("name")

  val (name: String, funcClass: String) = data("name").split("_").toList match {
    case "ui" :: xs => (toCamelCase("ui" :: xs), "vim")
    case x :: xs => (toCamelCase(xs), x)
    case _ => throw new IllegalArgumentException("unable to process name: " + data("name"))
  }

  val returnType: String = Function.getType(data("return_type")) match {
    case x if x == "Unit" => x
    case x => "Future[" + x + "]"
  }

  val params: List[List[String]] = data("parameters").asInstanceOf[List[List[String]]]
}
