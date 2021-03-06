/*******************************************************************************
 * Copyright (c) 2012, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.expressions;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.utilbox.collections.ArrayView;
import melnorme.utilbox.core.CoreUtil;
import dtool.ast.definitions.IFunctionAttribute;
import dtool.ast.definitions.IFunctionParameter;
import dtool.ast.references.Reference;
import dtool.ast.statements.IFunctionBody;

public class ExpFunctionLiteral extends Expression {
	
	public final Boolean isFunctionKeyword;
	public final Reference retType;
	public final NodeVector<IFunctionParameter> fnParams;
	public final NodeVector<IFunctionAttribute> fnAttributes;
	public final IFunctionBody fnBody;
	public final Expression bodyExpression;
	
	public ExpFunctionLiteral(Boolean isFunctionKeyword, Reference retType, NodeVector<IFunctionParameter> fnParams,
			NodeVector<IFunctionAttribute> fnAttributes, IFunctionBody fnBody, Expression bodyExpression) {
		this.isFunctionKeyword = isFunctionKeyword;
		this.retType = parentize(retType);
		this.fnParams = parentize(fnParams);
		this.fnAttributes = parentize(fnAttributes);
		this.fnBody = parentize(fnBody);
		this.bodyExpression = parentize(bodyExpression);
		assertTrue(fnBody == null || bodyExpression == null); // only one of each
	}
	
	public final ArrayView<ASTNode> getParams_asNodes() {
		return CoreUtil.blindCast(fnParams);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.EXP_FUNCTION_LITERAL;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, retType);
		acceptVisitor(visitor, fnParams);
		acceptVisitor(visitor, fnAttributes);
		acceptVisitor(visitor, fnBody);
		acceptVisitor(visitor, bodyExpression);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new ExpFunctionLiteral(isFunctionKeyword, clone(retType), clone(fnParams), clone(fnAttributes), 
			clone(fnBody), clone(bodyExpression));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append(isFunctionKeyword == Boolean.TRUE, "function ");
		cp.append(isFunctionKeyword == Boolean.FALSE, "delegate ");
		cp.append(retType);
		cp.appendList("(", getParams_asNodes(), ",", ") ");
		cp.appendList(fnAttributes, " ", true);
		cp.append(fnBody);
		cp.append(" => ", bodyExpression);
	}
	
}