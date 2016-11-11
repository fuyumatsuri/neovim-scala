package xyz.aoei.generate

import treehugger.forest._
import definitions._
import treehuggerDSL._

object Generator {
  def parseApiInfo(apiInfo: Any) = apiInfo match {
    case x :: List(apiData) =>
      val api = apiData.asInstanceOf[Map[String, _]]

      val types: Map[String, Int] = api("types").asInstanceOf[Map[String, Int]]

      val functions: List[Function] = api("functions").asInstanceOf[List[Map[String, String]]] map { x => new Function(x) }

      val errorTypes = api("error_types")

      (functions, errorTypes, types)
  }

  def generateFunction(types: Map[String, Int])(function: Function) = {
    def isTypeParam(x: List[String]) = types contains Function.getType(x.head)

    // If the function doesn't return anything, we notify, otherwise request
    val packetType: Tree = Function.getType(function.returnType) match {
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

    // Build the function
    DEF(function.name, function.returnType) withParams funcParams :=
      packetType APPLY ( LIT(function.apiName) :: requestParams )
  }

  def generateClass(functions: List[Function], types: Map[String, Int]) = {
    CLASSDEF("TTest") withParents "Neovim" := BLOCK (
      functions map generateFunction(types)
    )
  }
}
