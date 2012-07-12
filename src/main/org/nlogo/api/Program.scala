// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.mutable.Buffer
import collection.mutable.LinkedHashMap
import collection.JavaConverters._

object Program {
  def applyJ(interfaceGlobals: java.util.List[String],
             is3D: Boolean) =
    new Program(interfaceGlobals.asScala.toSeq,
                is3D = is3D,
                turtlesOwn = AgentVariables.getImplicitTurtleVariables(is3D).toBuffer,
                patchesOwn = AgentVariables.getImplicitPatchVariables(is3D).toBuffer,
                linksOwn = AgentVariables.getImplicitLinkVariables.toBuffer)
  def applyS(interfaceGlobals: Seq[String] = Nil,
             is3D: Boolean = false) =
    new Program(interfaceGlobals = interfaceGlobals,
                is3D = is3D,
                turtlesOwn = AgentVariables.getImplicitTurtleVariables(is3D).toBuffer,
                patchesOwn = AgentVariables.getImplicitPatchVariables(is3D).toBuffer,
                linksOwn = AgentVariables.getImplicitLinkVariables.toBuffer)
}

case class Program private(
  interfaceGlobals: Seq[String] = Nil,
  userGlobals: Buffer[String] = Buffer(),
  is3D: Boolean = false,
  turtlesOwn: Buffer[String] = Buffer(),
  patchesOwn: Buffer[String] = Buffer(),
  linksOwn: Buffer[String] = Buffer(),
  // use a LinkedHashMap to store the breeds so that the Renderer can retrieve them in order of
  // definition, for proper z-ordering - ST 6/9/04
  // Using LinkedHashMap on the other maps isn't really necessary for proper functioning, but makes
  // writing unit tests easier - ST 1/19/09
  // Yuck on this AnyRef stuff -- should be cleaned up - ST 3/7/08, 6/17/11
  breeds: collection.mutable.Map[String, AnyRef] = new LinkedHashMap[String, AnyRef],
  breedsSingular: collection.mutable.Map[String, String] = new LinkedHashMap[String, String],
  linkBreeds: collection.mutable.Map[String, AnyRef] = new LinkedHashMap[String, AnyRef],
  linkBreedsSingular: collection.mutable.Map[String, String] = new LinkedHashMap[String, String],
  breedsOwn: collection.mutable.Map[String, Seq[String]] = new LinkedHashMap[String, Seq[String]],
  linkBreedsOwn: collection.mutable.Map[String, Seq[String]] = new LinkedHashMap[String, Seq[String]]) {

  def globals: Seq[String] =
    AgentVariables.getImplicitObserverVariables ++
      interfaceGlobals.map(_.toUpperCase) ++ userGlobals

  // for convenience of Java callers
  def breedsJ: java.util.Map[String, AnyRef] = breeds.asJava
  def breedsSingularJ: java.util.Map[String, String] = breedsSingular.asJava
  def linkBreedsJ: java.util.Map[String, AnyRef] = linkBreeds.asJava
  def linkBreedsSingularJ: java.util.Map[String, String] = linkBreedsSingular.asJava
  def breedsOwnJ: java.util.Map[String, Seq[String]] = breedsOwn.asJava
  def linkBreedsOwnJ: java.util.Map[String, Seq[String]] = linkBreedsOwn.asJava

  // for debugging
  def dump = {
    def seq(xs: Seq[_]) =
      xs.mkString("[", " ", "]")
    def map(xs: collection.Map[_, _]) =
      xs.map(mapEntry)
        .mkString("", "\n", "\n")
        .trim
    def mapEntry[K, V](pair: (K, V)): String =
      pair._1.toString + " = " +
        (pair._2 match {
          case xs: Seq[_] => xs.mkString("[", ", ", "]")
          case x => x.toString
        })
    "globals " + seq(globals) + "\n" +
      "interfaceGlobals " + seq(interfaceGlobals) + "\n" +
      "turtles-own " + seq(turtlesOwn) + "\n" +
      "patches-own " + seq(patchesOwn) + "\n" +
      "links-own " + seq(linksOwn) + "\n" +
      "breeds " + map(breeds) + "\n" +
      "breeds-own " + map(breedsOwn) + "\n" +
      "link-breeds " + map(linkBreeds) + "\n" +
      "link-breeds-own " + map(linkBreedsOwn) + "\n"
  }

}
