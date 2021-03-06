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
package melnorme.lang.tooling.ast;

import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.utilbox.tree.IVisitable;

/**
 * Interface for {@link ASTNode} objects. No other class can implement. 
 */
public interface IASTNode extends ISourceElement, IVisitable<IASTVisitor>, ILanguageElement {
	
	public ASTNode asNode();
	
	/** Returns the parent of this node, or <code>null</code> if none. */
	@Override
	public ILanguageElement getLexicalParent();
	
	public void setParent(ASTNode newParent);
	
	/**
	 * Returns whether this element has one or more immediate children. This is
	 * a convenience method, and may be more efficient than testing whether
	 * <code>getChildren</code> is an empty array.
	 */
	boolean hasChildren();
	
	/** Returns the node's children. */
	public IASTNode[] getChildren();
	
	/** Clone this node, and all the sub-node in its sub-tree. */
	CommonASTNode cloneTree();
	
}