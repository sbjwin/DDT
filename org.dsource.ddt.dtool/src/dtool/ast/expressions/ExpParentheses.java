package dtool.ast.expressions;

import static dtool.util.NewUtils.assertNotNull_;
import melnorme.utilbox.tree.TreeVisitor;
import dtool.ast.ASTCodePrinter;
import dtool.ast.ASTNodeTypes;
import dtool.ast.IASTVisitor;
import dtool.ast.SourceRange;

public class ExpParentheses extends Expression {
	
	public final Expression exp;
	
	public ExpParentheses(Expression exp, SourceRange sourceRange) {
		initSourceRange(sourceRange);
		this.exp = parentize(assertNotNull_(exp));
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.EXP_PARENTHESES;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, exp);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.appendNode("(", exp);
		cp.append(")");
	}
}