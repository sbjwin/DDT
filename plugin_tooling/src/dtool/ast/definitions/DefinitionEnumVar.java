/*******************************************************************************
 * Copyright (c) 2013, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.definitions;


import static dtool.util.NewUtils.assertCast;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.scoping.INonScopedContainer;
import melnorme.utilbox.misc.IteratorUtil;
import dtool.ast.declarations.IDeclaration;
import dtool.ast.expressions.IInitializer;
import dtool.ast.references.Reference;
import dtool.ast.statements.IStatement;
import dtool.engine.analysis.CommonDefVarSemantics;
import dtool.engine.analysis.IVarDefinitionLike;

/**
 * A definition of an enum variable (aka manifest constant):
 * This is different from normal variables as some additional syntaxes are allowed, 
 * such as template parameters
 */
public class DefinitionEnumVar extends ASTNode implements IDeclaration, IStatement, INonScopedContainer {
	
	public final NodeVector<DefinitionEnumVarFragment> defFragments;

	public DefinitionEnumVar(NodeVector<DefinitionEnumVarFragment> defFragments) {
		this.defFragments = parentize(assertNotNull(defFragments));
		assertTrue(defFragments.size() > 0);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.DEFINITION_ENUM_VAR;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, defFragments);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new DefinitionEnumVar(clone(defFragments));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("enum ");
		cp.appendList(defFragments, ", ", false);
		cp.append(";");
	}
	
	public boolean isOffsetAtEnumKeyword(int offset) {
		return offset >= getStartPos() && offset <= getStartPos() + "enum".length();
	}
	
	@Override
	public Iterable<? extends IASTNode> getMembersIterable() {
		return IteratorUtil.nonNullIterable(defFragments);
	}
	
	public static class DefinitionEnumVarFragment extends DefUnit implements IVarDefinitionLike {
		
		public final NodeVector<ITemplateParameter> tplParams; // Since 2.064
		public final IInitializer initializer;
		
		public DefinitionEnumVarFragment(DefSymbol defName, NodeVector<ITemplateParameter> tplParams, 
				IInitializer initializer) {
			super(defName);
			this.tplParams = parentize(tplParams);
			this.initializer = parentize(initializer);
		}
		
		@Override
		public DefinitionEnumVar getParent_Concrete() {
			return assertCast(getParent(), DefinitionEnumVar.class);
		}
		
		@Override
		public ASTNodeTypes getNodeType() {
			return ASTNodeTypes.DEFINITION_ENUM_VAR_FRAGMENT;
		}
		
		@Override
		public void visitChildren(IASTVisitor visitor) {
			acceptVisitor(visitor, defName);
			acceptVisitor(visitor, tplParams);
			acceptVisitor(visitor, initializer);
		}
		
		@Override
		protected CommonASTNode doCloneTree() {
			return new DefinitionEnumVarFragment(clone(defName), clone(tplParams), clone(initializer));
		}
		
		@Override
		public void toStringAsCode(ASTCodePrinter cp) {
			cp.append(defName);
			cp.appendList("(", tplParams, ",", ") ");
			cp.append(" = ", initializer);
		}
		
		@Override
		public EArcheType getArcheType() {
			return EArcheType.Variable;
		}
		
		@Override
		public Reference getDeclaredType() {
			return null; // Never has one
		}
		
		@Override
		public IInitializer getDeclaredInitializer() {
			return initializer;
		}
		
		/* -----------------  ----------------- */
		
		@Override
		protected CommonDefVarSemantics doCreateSemantics(PickedElement<?> pickedElement) {
			return new CommonDefVarSemantics(this, pickedElement);
		}
		
	}
	
}