/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.definitions;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.context.ISemanticContext;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import melnorme.lang.tooling.symbols.IConcreteNamedElement;
import dtool.ast.definitions.TemplateAliasParam.TemplateAliasParamSemantics;
import dtool.ast.expressions.Resolvable;
import dtool.engine.analysis.templates.AliasElement;

public class TemplateThisParam extends DefUnit implements ITemplateParameter, IConcreteNamedElement {
	
	public TemplateThisParam(DefSymbol defName) {
		super(defName);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.TEMPLATE_THIS_PARAM;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, defName);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new TemplateThisParam(clone(defName));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("this ", defName);
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Alias;
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new TemplateAliasParamSemantics(this, pickedElement);
	}
	
	@Override
	public AliasElement createTemplateArgument(Resolvable resolvable, ISemanticContext tplRefContext) {
		return null;
	}
	
}