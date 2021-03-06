package mmrnmhrm.core.search;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;

import melnorme.lang.tooling.ast.ASTVisitor;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.utilbox.misc.PathUtil.InvalidPathExceptionX;
import mmrnmhrm.core.DLTKUtils;
import mmrnmhrm.core.engine_client.DToolClient;
import mmrnmhrm.core.model_elements.DeeModelEngine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParent;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.ISourceModule;

import dtool.ast.definitions.Module;
import dtool.parser.DeeParserResult.ParsedModule;

public class DeeSearchEngineTestUtils {
	
	public static String getSourceModuleFQName(ISourceModule sourceModule) {
		String moduleFileName = sourceModule.getElementName();
		int fileExtensionIx = moduleFileName.indexOf('.');
		if(fileExtensionIx != -1) {
			moduleFileName = moduleFileName.substring(0, fileExtensionIx);
		}
		String packageName = DeeModelEngine.getPackageName(sourceModule);
		return packageName.isEmpty() ? moduleFileName : packageName + "." + moduleFileName;
	}
	
	public static String getModelElementFQName(IMember element) {
		switch (element.getElementType()) {
		case IModelElement.FIELD:
		case IModelElement.METHOD:
		case IModelElement.TYPE:
		case IModelElement.LOCAL_VARIABLE:
			
			String parentFQName;
			if(element.getParent() instanceof IMember) {
				parentFQName = getModelElementFQName((IMember) element.getParent());
			} else {
				assertTrue(element.getParent() == element.getSourceModule());
				parentFQName = DeeModelEngine.getPackageName(element.getSourceModule());
			}
			
			String qualification = "";
			if(!parentFQName.isEmpty()) {
				qualification = parentFQName + ".";
			}
			return qualification + element.getElementName();
		default:
			throw assertFail();
		}
	}
	
	public static ISourceModule getSourceModule(IModelElement element) {
		return (ISourceModule) element.getAncestor(IModelElement.SOURCE_MODULE);
	}
	
	public static ArrayList<Integer> getNodeTreePath(ASTNode node) {

		ASTNode parent = node.getParent();
		if(parent == null) {
			return new ArrayList<Integer>();
		}
		
		ArrayList<Integer> parentPath = getNodeTreePath(parent);
		
		ASTNode[] children = parent.getChildren();
		for (int ix = 0; ix < children.length; ix++) {
			ASTNode child = children[ix];
			if(node == child) {
				parentPath.add(ix);
				assertTrue(getNodeFromPath(node.getModuleNode_(), parentPath) == node);
				return parentPath;
			}
		}
		throw assertFail();
	}
	
	public static ASTNode getNodeFromPath(ASTNode node, ArrayList<Integer> nodeTreePath) {
		return getNodeFromPath(node, nodeTreePath, 0);
	}
	
	private static ASTNode getNodeFromPath(ASTNode node, ArrayList<Integer> nodeTreePath, int treePathIx) {
		if(nodeTreePath.size() == treePathIx) {
			return node;
		}
		int ix = nodeTreePath.get(treePathIx);
		return getNodeFromPath(node.getChildren()[ix], nodeTreePath, treePathIx+1);
	}
	
	public static class ElementsAndDefUnitVisitor { 
		
		public void visitElementsAndNodes(IModelElement element, int depth) throws CoreException, 
		InvalidPathExceptionX {
			if(element instanceof ISourceModule) {
				final ISourceModule sourceModule = (ISourceModule) element;
				Path filePath = DLTKUtils.getFilePath(sourceModule);
				ParsedModule parseModule = DToolClient.getDefaultModuleCache().getParsedModuleOrNull(filePath);
				if(parseModule == null)
					return;
				
				Module module = parseModule.module;
				module.accept(new ASTVisitor() {
					@Override
					public boolean preVisit(ASTNode node) {
						visitNode(node, sourceModule);
						return true;
					}
				});
				
				return;
			}
			
			if(element instanceof IMember) {
				visitMember((IMember) element);
			}
			
			if(depth > 0 && element instanceof IParent) {
				if(element instanceof IProjectFragment && ((IProjectFragment) element).isExternal()) {
					return; // We do this to ignore standard library entry
				}
				IModelElement[] children = ((IParent) element).getChildren();
				for (IModelElement child : children) {
					visitElementsAndNodes(child, depth - 1);
				}
			}
		}
		
		@SuppressWarnings("unused")
		protected void visitNode(ASTNode node, ISourceModule sourceModule) {
		}
		
		@SuppressWarnings("unused")
		protected void visitMember(IMember element) throws CoreException {
		}
	}
	
}