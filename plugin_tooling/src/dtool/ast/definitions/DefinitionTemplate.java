/*******************************************************************************
 * Copyright (c) 2011, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.definitions;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import melnorme.lang.tooling.engine.scoping.IScopeElement;
import melnorme.lang.tooling.engine.scoping.ScopeTraverser;
import melnorme.lang.tooling.symbols.IConcreteNamedElement;
import dtool.ast.declarations.DeclBlock;
import dtool.ast.declarations.IDeclaration;
import dtool.ast.expressions.Expression;
import dtool.ast.expressions.MissingParenthesesExpression;
import dtool.ast.references.RefTemplateInstance;
import dtool.ast.statements.IStatement;
import dtool.engine.analysis.templates.DefTemplateSemantics;
import dtool.parser.common.Token;

/**
 * Definition of a template.
 * http://dlang.org/template.html#TemplateDeclaration
 * 
 * (Technically not allowed as statement, but parse so anyways.)
 */
public class DefinitionTemplate extends CommonDefinition 
	implements IScopeElement, IDeclaration, IStatement, ITemplatableElement, IConcreteNamedElement 
{
	
	public final boolean isMixin;
	protected final NodeVector<ITemplateParameter> tplParams;
	public final Expression tplConstraint;
	public final DeclBlock decls;
	
	public final boolean wrapper;
	
	public DefinitionTemplate(Token[] comments, boolean isMixin, DefSymbol defName, 
			NodeVector<ITemplateParameter> tplParams, Expression tplConstraint, DeclBlock decls) {
		super(comments, defName);
		this.isMixin = isMixin;
		this.tplParams = parentize(tplParams);
		this.tplConstraint = parentize(tplConstraint);
		this.decls = parentize(decls);
		
		this.wrapper = false; // TODO: determine this
		if(wrapper) {
			assertTrue(this.decls.nodes.size() == 1);
			assertTrue(decls.nodes.get(0) instanceof DefUnit);
		}
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.DEFINITION_TEMPLATE;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, defName);
		acceptVisitor(visitor, tplParams);
		acceptVisitor(visitor, tplConstraint);
		acceptVisitor(visitor, decls);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new DefinitionTemplate(comments, isMixin, clone(defName), clone(tplParams), clone(tplConstraint), 
			clone(decls));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append(isMixin, "mixin ");
		cp.append("template ");
		cp.append(defName, " ");
		cp.appendList("(", tplParams, ",", ") ");
		tplConstraintToStringAsCode(cp, tplConstraint);
		cp.append(decls);
	}
	
	public static void tplConstraintToStringAsCode(ASTCodePrinter cp, Expression tplConstraint) {
		if(tplConstraint instanceof MissingParenthesesExpression) {
			cp.append("if", tplConstraint);
		} else {
			cp.append("if(", tplConstraint, ")");
		}
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Template;
	}
	
	@Override
	public boolean isTemplated() {
		return true;
	}
	
	@Override
	public NodeVector<ITemplateParameter> getTemplateParameters() {
		return getEffectiveParameters();
	}
	
	public NodeVector<ITemplateParameter> getEffectiveParameters() {
		return NodeVector.nullAsEmpty(tplParams);
	}
	
	@Override
	public DefUnit cloneTemplateElement(RefTemplateInstance templateRef) {
		return setParsedFromOther(
			new DefinitionTemplate_Instantiated(templateRef, comments, isMixin, clone(defName), clone(decls)), 
			this);
	}
	
	public static class DefinitionTemplate_Instantiated extends DefinitionTemplate {
		
		protected final RefTemplateInstance templateRef;
		
		public DefinitionTemplate_Instantiated(RefTemplateInstance templateRef, Token[] comments, boolean isMixin, 
				DefSymbol defName, DeclBlock decls) {
			super(comments, isMixin, defName, null, null, decls);
			this.templateRef = assertNotNull(templateRef);
		}
		
		@Override
		public String getExtendedName() {
			return getName() + templateRef.normalizedArgsToString();
		}
		
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new DefTemplateSemantics(this, pickedElement);
	}
	
	
	@Override
	public ScopeTraverser getScopeTraverser() {
		return new ScopeTraverser(tplParams, false);
	}
	
}