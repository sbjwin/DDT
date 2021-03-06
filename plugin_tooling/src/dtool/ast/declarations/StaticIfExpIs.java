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
package dtool.ast.declarations;

import static dtool.util.NewUtils.assertCast;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.AliasSemantics.TypeAliasSemantics;
import melnorme.lang.tooling.engine.resolver.IReference;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import dtool.ast.definitions.DefSymbol;
import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.EArcheType;
import dtool.ast.definitions.ITemplateParameter;
import dtool.ast.expressions.ExpIs;
import dtool.ast.expressions.ExpIs.ExpIsSpecialization;
import dtool.ast.expressions.Expression;
import dtool.ast.references.Reference;

public class StaticIfExpIs extends Expression {
	
	public final Reference typeRef;
	public final StaticIfExpIsDefUnit isExpDefUnit;
	public final ExpIsSpecialization specKind;
	public final Reference specTypeRef;
	public final NodeVector<ITemplateParameter> tplParams;
	
	public StaticIfExpIs(Reference typeRef, StaticIfExpIsDefUnit isExpDefUnit, ExpIsSpecialization specKind, 
		Reference specTypeRef, NodeVector<ITemplateParameter> tplParams) {
		this.typeRef = parentize(assertNotNull(typeRef));
		this.isExpDefUnit = parentize(isExpDefUnit);
		this.specKind = specKind;
		this.specTypeRef = parentize(specTypeRef);
		assertTrue((specTypeRef == null) ==
			(specKind != ExpIsSpecialization.TYPE_SUBTYPE && specKind != ExpIsSpecialization.TYPE_EXACT)); 
		this.tplParams = parentize(tplParams);
		assertTrue((tplParams == null) ? true :
			(specKind == ExpIsSpecialization.TYPE_SUBTYPE || specKind == ExpIsSpecialization.TYPE_EXACT)); 
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.STATIC_IF_EXP_IS;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, typeRef);
		acceptVisitor(visitor, isExpDefUnit);
		acceptVisitor(visitor, specTypeRef);
		acceptVisitor(visitor, tplParams);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new StaticIfExpIs(clone(typeRef), clone(isExpDefUnit), specKind, clone(specTypeRef), clone(tplParams));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("is(");
		cp.append(typeRef, " ");
		cp.append(isExpDefUnit);
		ExpIs.toStringAsCode_isExpSpecKind(cp, specKind, specTypeRef);
		cp.appendList(", ", tplParams, ", ", "");
		cp.append(")");
	}
	
	@Override
	public void doCompleteNodeAnalysis() {
		if(!(getParent() instanceof DeclarationStaticIf)) {
			// TODO add error
		}
	}
	
	public static class StaticIfExpIsDefUnit extends DefUnit {
		
		public StaticIfExpIsDefUnit(DefSymbol defName) {
			super(defName);
		}
		
		@Override
		protected StaticIfExpIs getParent_Concrete() {
			return assertCast(parent, StaticIfExpIs.class);
		}
		
		@Override
		public ASTNodeTypes getNodeType() {
			return ASTNodeTypes.STATIC_IF_EXP_IS_DEF_UNIT;
		}
		
		@Override
		public void visitChildren(IASTVisitor visitor) {
			acceptVisitor(visitor, defName);
		}
		
		@Override
		protected CommonASTNode doCloneTree() {
			return new StaticIfExpIsDefUnit(clone(defName));
		}
		
		@Override
		public void toStringAsCode(ASTCodePrinter cp) {
			cp.append(defName);
		}
		
		@Override
		public EArcheType getArcheType() {
			return EArcheType.Alias;
		}
		
		/* -----------------  ----------------- */
		
		@Override
		protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
			return new TypeAliasSemantics(this, pickedElement) {
			
			@Override
			protected IReference getAliasTarget() {
				return getParent_Concrete().typeRef;
			}
			
		};
		}
		
	}
	
}