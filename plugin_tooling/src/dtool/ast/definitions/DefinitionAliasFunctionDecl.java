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
package dtool.ast.definitions;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import melnorme.lang.tooling.engine.scoping.CommonScopeLookup;
import melnorme.lang.tooling.symbols.IConcreteNamedElement;
import melnorme.lang.tooling.symbols.INamedElement;
import melnorme.utilbox.collections.ArrayView;
import melnorme.utilbox.core.CoreUtil;
import dtool.ast.declarations.Attribute;
import dtool.ast.definitions.DefinitionFunction.AbstractFunctionElementSemantics;
import dtool.ast.references.Reference;
import dtool.ast.statements.IStatement;
import dtool.parser.common.Token;

/**
 * A definition of an alias, in the old syntax:
 * <code>alias BasicType Declarator ;</code>
 * when Declarator declares a function.
 * 
 * @see http://dlang.org/declaration.html#AliasDeclaration
 */
public class DefinitionAliasFunctionDecl extends CommonDefinition implements IStatement {
	
	public final NodeVector<Attribute> aliasedAttributes;
	public final Reference target;
	public final NodeVector<IFunctionParameter> fnParams;
	public final NodeVector<IFunctionAttribute> fnAttributes;
	
	public DefinitionAliasFunctionDecl(Token[] comments, NodeVector<Attribute> aliasedAttributes, Reference target, 
			DefSymbol defName, NodeVector<IFunctionParameter> fnParams, NodeVector<IFunctionAttribute> fnAttributes) {
		super(comments, defName);
		this.aliasedAttributes = parentize(aliasedAttributes);
		this.target = parentize(target);
		this.fnParams = parentize(fnParams);
		this.fnAttributes = parentize(fnAttributes);
		assertTrue(fnAttributes == null || fnParams != null);
	}
	
	public final ArrayView<ASTNode> getParams_asNodes() {
		return CoreUtil.blindCast(fnParams);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.DEFINITION_ALIAS_FUNCTION_DECL;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, aliasedAttributes);
		acceptVisitor(visitor, target);
		acceptVisitor(visitor, defName);
		acceptVisitor(visitor, fnParams);
		acceptVisitor(visitor, fnAttributes);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new DefinitionAliasFunctionDecl(comments, clone(aliasedAttributes), clone(target), clone(defName), 
			clone(fnParams), clone(fnAttributes));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("alias ");
		cp.appendList(aliasedAttributes, " ", true);
		cp.append(target, " ");
		cp.append(defName);
		cp.appendList("(", getParams_asNodes(), ",", ") ");
		cp.appendList(fnAttributes, " ", true);
		cp.append(";");
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Alias;
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new FunctionalAliasSemantics(this, pickedElement);
	}
	
	public class FunctionalAliasSemantics extends AbstractFunctionElementSemantics {
		
		public FunctionalAliasSemantics(INamedElement element, PickedElement<?> pickedElement) {
			super(element, pickedElement);
		}
		
		@Override
		protected IConcreteNamedElement doResolveConcreteElement() {
			return null; // TODO: test and implement
		}
		
		@Override
		public void resolveSearchInMembersScope(CommonScopeLookup search) {
			resolveSearchInMembersScopeForFunction(search, target);
		}
	}
	
}