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
package dtool.ast.references;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.scoping.CommonScopeLookup;

public class RefIdentifier extends CommonRefIdentifier implements ITemplateRefNode {
	
	public RefIdentifier(String identifier) {
		super(identifier);
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.REF_IDENTIFIER;
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new RefIdentifier(identifier);
	}
	
	@Override
	public void performNameLookup(CommonScopeLookup search) {
		// Check if we are rooted in the lexical scope, or in a qualification reference
		CommonQualifiedReference qualificationRef = getQualificationReference();
		if(qualificationRef != null) {
			qualificationRef.performQualifiedRefSearch(search);
		} else {
			super.performNameLookup(search);
		}
	}
	
	protected CommonQualifiedReference getQualificationReference() {
		if(getParent() instanceof CommonQualifiedReference) {
			CommonQualifiedReference parent = (CommonQualifiedReference) getParent();
			if(parent.getQualifiedName() == this) {
				return parent;
			}
		}
		return null;
	}
	
}