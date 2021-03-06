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

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.ExpSemantics;
import melnorme.lang.tooling.engine.resolver.TypeReferenceResult;

public class ExpCastQual extends Expression {
	
	public static enum CastQualifiers {
		CONST("const"),
		CONST_SHARED("const shared"),
		INOUT("inout"),
		INOUT_SHARED("inout shared"),
		SHARED("shared"),
		SHARED_CONST("shared const"),
		SHARED_INOUT("shared inout"),
		IMMUTABLE("immutable"),
		;
		
		private String sourceValue;
		
		private CastQualifiers(String sourceValue) {
			this.sourceValue = sourceValue;
		}
		public String toStringAsCode() {
			return sourceValue;
		}
	}
	
	public final CastQualifiers castQualifier;
	public final Expression exp;
	
	public ExpCastQual(CastQualifiers castQualifier, Expression exp) {
		this.castQualifier = assertNotNull(castQualifier);
		this.exp = parentize(exp);
		assertTrue(exp == null || castQualifier != null);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.EXP_CAST_QUAL;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, exp);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new ExpCastQual(castQualifier, clone(exp));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append("cast");
		cp.appendStrings("(", castQualifier.toStringAsCode(), ")");
		cp.append(exp);
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected ExpSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new ExpSemantics(this, pickedElement) {
			
			@Override
			public TypeReferenceResult doCreateExpResolution() {
				if(exp == null) 
					return null;
				TypeReferenceResult baseExpType = exp.resolveTypeOfUnderlyingValue_nonNull(context);
				// TODO: should modify baseExpType with modifiers
				return baseExpType;
			}
			
		};
	}
	
}