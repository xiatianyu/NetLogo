// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.api.Syntax

class _netlogoweb extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.BooleanType)
  override def report(context: Context) =
    java.lang.Boolean.FALSE
}
