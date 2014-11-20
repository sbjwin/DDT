/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.ast;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.NodeData.CreatedStatusNodeData;
import melnorme.lang.tooling.ast.NodeData.ParsedNodeData;
import melnorme.lang.tooling.ast.util.ASTChildrenCollector;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.ASTDirectChildrenVisitor;
import melnorme.lang.tooling.ast.util.NodeUtil;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.bundles.ISemanticContext;
import melnorme.lang.tooling.engine.IElementSemantics;
import melnorme.lang.tooling.engine.scoping.IScopeNode;
import melnorme.utilbox.collections.ArrayView;
import melnorme.utilbox.core.CoreUtil;
import dtool.engine.IModuleResolution;
import dtool.parser.ParserError;
import dtool.resolver.ReferenceResolver;

public abstract class CommonASTNode implements IASTNode {
	
	public static final ASTNode[] NO_ELEMENTS = new ASTNode[0]; 
	
	/** Source range start position */
	protected int sourceStart = -1;
	/** Source range end position */
	protected int sourceEnd = -1;
	
	/** AST node parent, null if the node is the tree root. */
	protected ASTNode parent = null;
	/** Custom field to store various kinds of data */
	private NodeData data = NodeData.CREATED_STATUS; 
	
	
	public CommonASTNode() {
	}
	
	@Override
	public final ASTNode asNode() {
		return (ASTNode) this;
	}
	
	/* ------------------------  Source range ------------------------ */
	
	
	/** Gets the source range start position. */
	@Override
	public final int getStartPos() {
		assertTrue(hasSourceRangeInfo());
		return sourceStart;
	}
	
	/** Gets the source range end position. */
	@Override
	public final int getEndPos() {
		assertTrue(hasSourceRangeInfo());
		return sourceEnd;
	}
	
	/** Gets the source range start position, aka offset. */
	@Override
	public final int getOffset() {
		assertTrue(hasSourceRangeInfo());
		return sourceStart;
	}
	
	/** Gets the source range length. */
	@Override
	public final int getLength() {
		assertTrue(hasSourceRangeInfo());
		return sourceEnd - sourceStart;
	}
	
	public final SourceRange getSourceRange() {
		assertTrue(hasSourceRangeInfo());
		return new SourceRange(getStartPos(), getLength());
	}
	
	public final SourceRange getSourceRangeOrNull() {
		if(hasSourceRangeInfo()) {
			return getSourceRange();
		}
		return null;
	}
	
	/** Checks if the node has source range info. */
	public final boolean hasSourceRangeInfo() {
		return this.sourceStart != -1;
	}
	
	/** Sets the source positions, which must be valid. */
	public final void setSourcePosition(int startPos, int endPos) {
		assertTrue(!hasSourceRangeInfo()); // Can only be set once
		assertTrue(startPos >= 0);
		assertTrue(endPos >= startPos);
		this.sourceStart = startPos;
		this.sourceEnd = endPos;
	}
	
	/** Sets the source range of the receiver to given startPositon and given length */
	public final void setSourceRange(int startPosition, int length) {
		setSourcePosition(startPosition, startPosition + length);
	}
	
	/** Sets the source range according to given sourceRange. */
	public final void setSourceRange(SourceRange sourceRange) {
		setSourcePosition(sourceRange.getOffset(), sourceRange.getOffset() + sourceRange.getLength());
	}
	
	/* ------------------------  Parent and children visitor ------------------------ */
	
	@Override
	public final ASTNode getParent() {
		return parent;
	}
	
	/** Set the parent of this node. Cannot be null. Cannot set parent twice without explicitly detaching. */
	@Override
	public final void setParent(ASTNode parent) {
		assertTrue(parent != null);
		assertTrue(this.parent == null);
		this.parent = parent;
		checkNewParent();
	}
	
	protected void checkNewParent() {
		// Default implementation: do nothing
		// subclasses can implement to check a contract relating to their parent 
		// (usually, to ensure the parent is of a certain class)
		getParent_Concrete();
	}
	
	/** Same as {@link #getParent()}, but allows classes to cast to a more specific parent. */
	// Is this extra method really needed instead of just defining getParent as non-final?
	// Would the casts make a different in performance?
	protected ASTNode getParent_Concrete() {
		return getParent();
	}
	
	public void detachFromParent() {
		assertNotNull(this.parent);
		CommonASTNode parent_ = (CommonASTNode) this.parent;
		parent_.data = null; // Note, parent becomes an invalid node after this.
		this.parent = null;
	}
	
	/** Accept a visitor into this node. */
	@Override
	public final void accept(IASTVisitor visitor) {
		assertNotNull(visitor);
		
		// begin with the generic pre-visit
		if(visitor.preVisit(asNode())) {
			visitChildren(visitor);
		}
		// end with the generic post-visit
		visitor.postVisit(asNode());
	}
	
	public abstract void visitChildren(IASTVisitor visitor);
	
	public void visitDirectChildren(ASTDirectChildrenVisitor directChildrenVisitor) {
		accept(directChildrenVisitor); // This might be optimized in the future
	}
	
	@Override
	public boolean hasChildren() {
		CheckForChildrenVisitor checkForChildrenVisitor = new CheckForChildrenVisitor();
		visitDirectChildren(checkForChildrenVisitor);
		return checkForChildrenVisitor.hasChildren;
	}
	
	public static final class CheckForChildrenVisitor extends ASTDirectChildrenVisitor {
		boolean hasChildren = false;
		
		@Override
		protected void geneticChildrenVisit(ASTNode child) {
			hasChildren = true;
		}
	}
	
	@Override
	public ASTNode[] getChildren() {
		return ASTChildrenCollector.getChildrenArray(asNode());
	}
	
	// Utility methods
	
	/** Accepts the visitor on child. If child is null, nothing happens. */
	public static void acceptVisitor(IASTVisitor visitor, IASTNode node) {
		if (node != null) {
			node.accept(visitor);
		}
	}
	
	/** Accepts the visitor on the children. If children is null, nothing happens. */
	public static void acceptVisitor(IASTVisitor visitor, Iterable<? extends IASTNode> nodes) {
		if (nodes == null)
			return;
		
		for(IASTNode node : nodes) {
			acceptVisitor(visitor, node);
		}
	}
	
	/* ------------------------  Node data ------------------------  */
	
	public final NodeData getData() {
		return assertNotNull(data);
	}
	
	/** Set the data of this node. Cannot be null. Cannot set data twice without explicitly resetting */
	public final void setData(NodeData data) {
		assertNotNull(data);
		this.data = data;
	}
	
	/** Removes the data of this node. Can only remove data if node is in parsed status. 
	 * @return the previous data. */
	public NodeData resetData() {
		assertTrue(isParsedStatus()); // can only remove data if node is in parsed status
		NodeData oldData = data;
		this.data = NodeData.CREATED_STATUS;
		return oldData;
	}
	
	protected CreatedStatusNodeData getDataAtCreatedPhase() {
		assertTrue(data == NodeData.CREATED_STATUS); 
		//return (ParsedNodeData) this.data;
		return NodeData.CREATED_STATUS;
	}
	
	protected ParsedNodeData getDataAtParsedPhase() {
		assertTrue(data.isParsedStatus()); 
		return (ParsedNodeData) data;
	}
	
	public void setParsedStatus() {
		getDataAtCreatedPhase().setParsed(asNode());
	}
	
	public void setParsedStatusWithErrors(ParserError... errors) {
		getDataAtCreatedPhase().setParsedWithErrors(asNode(), errors);
	}
	
	public final boolean isParsedStatus() {
		return getData().isParsedStatus();
	}
	
	/* =============== STRING FUNCTIONS =============== */
	
	/** Gets the node's classname striped of package qualifier,  plus optional range info. */
	@Override
	public final String toStringAsNode(boolean printRangeInfo) {
		String str = toStringClassName();
		
		if(printRangeInfo) {
			str += " ["+ getStartPos() +"+"+ getLength() +"]";
		}
		return str;
	}
	
	/** Gets the node's classname striped of package qualifier. */
	public final String toStringClassName() {
		String str = this.getClass().getName();
		int lastIx = str.lastIndexOf('.');
		return str.substring(lastIx+1);
	}
	
	@Override
	public final String toString() {
		StringBuilder string = new StringBuilder();
		string.append(toStringClassName());
		string.append(isParsedStatus() ? "#" : ":" + getData());
		
		string.append(toStringAsCode());
		string.append("\n");
		return string.toString(); 
	}
	
	/** Returns a source representation of this node. 
	 * If node parsed without errors then this representation should be equal 
	 * to the original parsed source (disregarding sub-channel tokens).
	 * Otherwise, if there were errors, this method should still try to print something as close as possible
	 * to the original parsed source: 
	 * All tokens that were consumed should be printed.
	 * Expected tokens that were *not* consumed should preferably be printed as well, but it is not strictly required. 
	 */
	public final String toStringAsCode() {
		ASTCodePrinter cp = new ASTCodePrinter();
		toStringAsCode(cp);
		return cp.toString();
	}
	
	/** @see #toStringAsCode() */
	public abstract void toStringAsCode(ASTCodePrinter cp);
	
	/* =============== Parenting utils =============== */
	
	public static <T> ArrayView<T> nonNull(ArrayView<T> arrayView) {
		return arrayView != null ? arrayView : ArrayView.EMPTY_ARRAYVIEW.<T>upcastTypeParameter();
	}
	
	/** Set the parent of the given collection to the receiver. @return collection */
	protected <T extends ArrayView<? extends ASTNode>> T parentize(T collection) {
		return parentize(collection, false);
	}
	
	protected <T extends ArrayView<? extends ASTNode>> T parentize(T collection, boolean allowNulls) {
		if (collection != null) {
			for (ASTNode node : collection) {
				if(node != null) {
					node.setParent(asNode());
				} else {
					assertTrue(allowNulls);
				}
			}
		}
		return collection;
	}
	
	/** Set the parent of the given node to the receiver. @return node */
	protected <T extends IASTNode> T parentize(T node) {
		if (node != null) {
			node.setParent(asNode());
		}
		return node;
	}
	
	protected <T extends IASTNode> T parentizeI(T node) {
		return parentize(node);
	}
	
	protected <T extends IASTNode> ArrayView<T> parentizeI(ArrayView<T> collection) {
		parentize(CoreUtil.<ArrayView<ASTNode>>blindCast(collection), false);
		return collection;
	}
	
	/* =============== Analysis and semantics =============== */
	
	public static void doSimpleAnalysisOnTree(ASTNode treeNode) {
		ASTVisitor childrenVisitor = new LocalAnalysisVisitor();
		treeNode.accept(childrenVisitor);
	}
	
	protected static final class LocalAnalysisVisitor extends ASTVisitor {
		@Override
		public boolean preVisit(ASTNode node) {
			node.doNodeSimpleAnalysis();
			return true;
		}
		
		@Override
		public void postVisit(ASTNode node) {
			node.endNodeSimpleAnalysis();
		}
	}
	
	
	public void doNodeSimpleAnalysis() {
		assertTrue(isParsedStatus());
		// Default implementation: do nothing
	}
	
	public void endNodeSimpleAnalysis() {
		getDataAtParsedPhase().setLocallyAnalysedData(asNode());
	}
	
	public boolean isPostParseStatus() {
		return getData().isLocallyAnalyzedStatus();
	}
	
	/* ------------------------------------------------------------ */
	
	public final IModuleNode getModuleNode() {
		return NodeUtil.getMatchingParent(this, IModuleNode.class);
	}
	
	public IScopeNode getOuterLexicalScope() {
		return ReferenceResolver.getOuterLexicalScope(asNode());
	}
	
	public IElementSemantics getNodeSemantics() {
		return IElementSemantics.NULL_NODE_SEMANTICS;
	}
	
	@Override
	public IElementSemantics getSemantics(ISemanticContext br) {
		/*FIXME: BUG here todo*/
		return IElementSemantics.NULL_NODE_SEMANTICS;
	}
	
	protected IElementSemantics doGetSemantics(ISemanticContext br) {
		IModuleNode moduleNode = getModuleNode();
		if(moduleNode == null) {
			return null;
		}
		
		IModuleResolution moduleSemantics = br.findSemanticsContainer(moduleNode);
		
		IElementSemantics elementSemantics = moduleSemantics.getElementSemantics(this);
		
		if(elementSemantics != null) {
			return elementSemantics;
		} else {
			return moduleSemantics.putElementSemantics(this, createElementSemantics());
		}
	}
	
	protected IElementSemantics createElementSemantics() {
		return IElementSemantics.NULL_NODE_SEMANTICS;
	}
	
}