// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import com.tristanhunt.knockoff.DefaultDiscounter._
import java.io.InputStream

object InfoFormatter {

  type MarkDownString = String
  type HTML = String
  type CSS = String

  /**
   * for standalone use, for example on a web server
   */
  def main(argv: Array[String]) {
    println(apply(read(System.in)))
  }
  def read(in:InputStream): String = io.Source.fromInputStream(in).mkString

  def styleSheetFile: CSS = org.nlogo.util.Utils.getResourceAsString("/system/info.css")
  val defaultFontSize = 14
  val defaultStyleSheet: CSS = styleSheet(defaultFontSize)
  def styleSheet(fontSize: Int): CSS = "<style type=\"text/css\">\n<!--\n"+
          styleSheetFile.
            replace("{BODY-FONT-SIZE}", fontSize.toString).
            replace("{H1-FONT-SIZE}", (fontSize * 1.5).toInt.toString).
            replace("{H2-FONT-SIZE}", (fontSize * 1.25).toInt.toString).
            replace("{H3-FONT-SIZE}", fontSize.toString) + "\n-->\n</style>"

  // Knockoff fails to do some of the nice things that PegDown did (like autolinks), but... whatever; works with WebStart. --JAB (10/16/12)
  def toInnerHtml(str: MarkDownString): HTML = toXHTML(knockoff(str)).toString()

  def wrapHtml(body: HTML, fontSize:Int=defaultFontSize): HTML = {
    "<html><head>"+styleSheet(fontSize)+"</head><body>"+body+"</body></html>"
  }

  def apply(content:String, fontSize:Int=defaultFontSize, attachModelDir:String=>String=identity) = {
    wrapHtml(toInnerHtml(content), fontSize)
  }

}
