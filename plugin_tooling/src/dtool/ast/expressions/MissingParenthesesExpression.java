/*******************************************************************************
 * Copyright (c) 2012, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.expressions;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;

/** 
 * This class represents a syntax error where an expression delimited by parentheses was expected.
 * It is used instead of null so that it can provide the source range of where the parentheses were expected. 
 */
public class MissingParenthesesExpression extends Expression {
	
	public MissingParenthesesExpression() {
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.MISSING_EXPRESSION;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new MissingParenthesesExpression();
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("/*( MissingExp )*/");
	}
	
	public static void appendParenthesesExp(ASTCodePrinter cp, Expression expression) {
		if(expression instanceof MissingParenthesesExpression) {
			cp.append(expression);
		} else {
			cp.append("(", expression, ") ");
		}
	}
	
}