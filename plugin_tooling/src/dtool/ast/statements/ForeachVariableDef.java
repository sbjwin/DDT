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
package dtool.ast.statements;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.IReference;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import melnorme.lang.tooling.engine.resolver.VarSemantics;
import melnorme.lang.tooling.symbols.IConcreteNamedElement;
import dtool.ast.definitions.DefSymbol;
import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.EArcheType;
import dtool.ast.references.Reference;
import dtool.parser.common.LexElement;

public class ForeachVariableDef extends DefUnit implements IConcreteNamedElement {
	
	public final boolean isRef;
	public final LexElement typeMod;
	public final Reference type;
	
	public ForeachVariableDef(boolean isRef, LexElement typeMod, Reference type, DefSymbol defName) {
		super(defName);
		this.isRef = isRef;
		this.typeMod = typeMod;
		this.type = parentize(type);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.FOREACH_VARIABLE_DEF;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, type);
		acceptVisitor(visitor, defName);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new ForeachVariableDef(isRef, typeMod, clone(type), clone(defName));
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append(isRef, "ref ");
		cp.appendToken(typeMod, " ");
		cp.append(type, " ");
		cp.append(defName);
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Variable;
	}
	
	/* -----------------  ----------------- */
	
	@Override
	protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new VarSemantics(this, pickedElement) {
			
			@Override
			protected IReference getTypeReference() {
				return type;
			};
			
		};
	}
	
}