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
import dtool.parser.DeeTokens;

public class ExpPrefix extends Expression {
	
	public static enum EPrefixOpType {
		ADDRESS(DeeTokens.AND),
		PRE_INCREMENT(DeeTokens.INCREMENT),
		PRE_DECREMENT(DeeTokens.DECREMENT),
		REFERENCE(DeeTokens.STAR),
		NEGATIVE(DeeTokens.MINUS),
		POSITIVE(DeeTokens.PLUS),
		NOT(DeeTokens.NOT),
		COMPLEMENT(DeeTokens.CONCAT),
		
		DELETE(DeeTokens.KW_DELETE),
		;
		
		public final DeeTokens token;
		
		private EPrefixOpType(DeeTokens token) {
			this.token = token;
			assertTrue(token.hasSourceValue());
		}
		
		private static final EPrefixOpType[] mapping = initMapping(EPrefixOpType.values());
		
		private static EPrefixOpType[] initMapping(EPrefixOpType[] tokenEnum) {
			EPrefixOpType[] mappingArray = new EPrefixOpType[DeeTokens.values().length];
			for (EPrefixOpType prefixOpType : tokenEnum) {
				int ix = prefixOpType.token.ordinal();
				assertTrue(mappingArray[ix] == null);
				mappingArray[ix] = prefixOpType;
			}
			return mappingArray;
		}
		
		public static EPrefixOpType tokenToPrefixOpType(DeeTokens token) {
			return mapping[token.ordinal()];
		}
		
		public String getSourceValue() {
			return token.getSourceValue();
		}
		
	}
	
	public final EPrefixOpType kind;
	public final Expression exp;
	
	public ExpPrefix(EPrefixOpType kind, Expression exp) {
		this.kind = assertNotNull(kind);
		this.exp = parentize(exp);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.EXP_PREFIX;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, exp);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new ExpPrefix(kind, clone(exp));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.appendStrings(kind.getSourceValue(), " ");
		cp.append(exp);
	}
	
}