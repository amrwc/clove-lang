/* Generated By:JJTree: Do not edit this line. ASTOr.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=uk.ac.derby.ldi.calcast.calculator.BaseASTNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package uk.ac.derby.ldi.calcast.parser;

public
class ASTOr extends SimpleNode {
  public ASTOr(int id) {
    super(id);
  }

  public ASTOr(Calc p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CalcVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=444ed2ea3a0285da3f5cd02386068241 (do not edit this line) */
