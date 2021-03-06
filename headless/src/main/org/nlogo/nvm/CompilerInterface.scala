// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import api.{ Program, World, ExtensionManager, Token }
import collection.immutable.ListMap

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

object CompilerInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  type ProceduresMap = ListMap[String, Procedure]
  val NoProcedures: ProceduresMap = ListMap()
}

trait CompilerInterface {

  import CompilerInterface.ProceduresMap

  def compileProgram(source: String, program: Program, extensionManager: ExtensionManager,
    flags: CompilerFlags = CompilerFlags()): CompilerResults

  def compileMoreCode(source: String, displayName: Option[String], program: Program,
    oldProcedures: ProceduresMap, extensionManager: ExtensionManager,
    flags: CompilerFlags = CompilerFlags()): CompilerResults

  def checkCommandSyntax(source: String, program: Program, procedures: ProceduresMap,
                         extensionManager: ExtensionManager, parse: Boolean)

  def checkReporterSyntax(source: String, program: Program, procedures: ProceduresMap,
                          extensionManager: ExtensionManager, parse: Boolean)

  def autoConvert(source: String, subprogram: Boolean, reporter: Boolean, version: String,
                  workspace: AnyRef, ignoreErrors: Boolean, is3D: Boolean): String

  def readFromString(source: String, is3D: Boolean): AnyRef

  def readFromString(source: String, world: World, extensionManager: ExtensionManager, is3D: Boolean): AnyRef

  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager, is3D: Boolean): AnyRef

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef

  def findProcedurePositions(source: String, is3D: Boolean): Map[String, (String, Int, Int, Int)]
  def findIncludes(sourceFileName: String, source: String, is3D: Boolean): Map[String, String]
  def isValidIdentifier(s: String, is3D: Boolean): Boolean
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager, is3D: Boolean): Seq[Token]
}

case class CompilerFlags(
  foldConstants: Boolean = true,
  useGenerator: Boolean = api.Version.useGenerator,
  useOptimizer: Boolean = api.Version.useOptimizer)
