package xyz.aoei.neovim.bindings

import treehugger.forest._
import definitions._
import treehuggerDSL._

object Generator {
  def generate(data: Any) : String = {
    val imports = List(
      IMPORT("java.io", "InputStream", "OutputStream"),
      IMPORT("scala.concurrent", "Future"),
      IMPORT("xyz.aoei.msgpack.rpc", "Session", "ExtendedType")
    )
    val (functions, errorTypes, types) = Generator.parseApiInfo(data)

    val bindings = BLOCK(
      imports ::: Generator.generateClass(functions, types)
    ) inPackage "xyz.aoei.neovim"

    treeToString(bindings)
  }

  def parseApiInfo(apiInfo: Any) = apiInfo match {
    case x :: List(apiData) =>
      val api = apiData.asInstanceOf[Map[String, _]]

      val types: List[NvimType] = (api("types").asInstanceOf[Map[String, Map[String, String]]] toList) map {
        x: (String, Map[String, String]) => new NvimType(x._1, x._2)
      }

      val functions: List[Function] = api("functions").asInstanceOf[List[Map[String, String]]] map {
        x => new Function(x, types)
      }

      val errorTypes = api("error_types")

      (functions, errorTypes, types)
  }

  def generateFunction(function: Function) = {
    def isTypeParam(x: List[String]) = function.nvimType == Function.getType(x.head)

    // If the function doesn't return anything, we notify, otherwise request
    val packetType: Tree = function.requestType match {
      case "Unit" => REF("session") DOT "notify"
      case x => REF("session") DOT "request" APPLYTYPE x
    }

    // Strip out function call parameters that are the instance of the object
    // ex. window_get_height doesn't need to be passed a window object since
    // it is being called on the object
    val funcParams: List[ValDef] = function.params filterNot isTypeParam map {
      x => PARAM(x(1), Function.getType(x.head)): ValDef
    }

    val requestParams = function.params map {
      case x if isTypeParam(x) => REF(RootClass.newValue("this"))
      case x => REF(x(1))
    }

    val funcDef =
      if (funcParams.isEmpty && function.returnType != "Unit")DEF(function.name, function.returnType)
      else DEF(function.name, function.returnType) withParams funcParams

    // Build the function
      funcDef := packetType APPLY ( LIT(function.apiName) :: requestParams )
  }

  def generateClass(functions: List[Function], types: List[NvimType]) = {
    // filter out deprecated functions and group by type
    val functionGroups = functions filter (_.deprecated < 0) groupBy(_.nvimType)

    val typeRegistrations = types map generateTypeRegistration
    val typesOverride = DEFINFER("types") withFlags Flags.OVERRIDE := LIST(typeRegistrations)

    val mainFunctions = functionGroups("Nvim") map generateFunction
    val mainClass = (CLASSDEF("Neovim")
      withParams(VAL("in", "InputStream"), VAL("out", "OutputStream"))
      withParents "NeovimBase(in, out)" := BLOCK(typesOverride :: mainFunctions))

    mainClass :: (types map { x =>
      val classFunctions = functionGroups(x.name) map generateFunction
      (CLASSDEF(x.name)
        withParams(VAL("session", "Session"), VAL("data", "Array[Byte]"))
        withParents "TypeBase" := BLOCK(classFunctions))
    })
  }

  def generateTypeRegistration(nvimType: NvimType) = {
    val shortName = nvimType.name.toLowerCase.take(3)
    REF("ExtendedType") APPLY (REF("classOf") APPLYTYPE nvimType.name, REF(nvimType.id.toString),
      LAMBDA(PARAM(shortName, nvimType.name)) ==> (REF(shortName) DOT "data"),
      LAMBDA(PARAM("bytes", "Array[Byte]")) ==> NEW(nvimType.name, REF("session"), REF("bytes"))
      )
  }
}
