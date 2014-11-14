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
package dtool.engine.operations;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import melnorme.lang.tooling.ast.ASTNodeFinder;
import melnorme.lang.tooling.ast.INamedElementNode;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.ast_actual.ILangNamedElement;
import melnorme.lang.tooling.bundles.IModuleResolver;
import dtool.ast.definitions.DefSymbol;
import dtool.ast.definitions.Module;
import dtool.ast.references.CommonQualifiedReference;
import dtool.ast.references.NamedReference;
import dtool.ast.references.Reference;
import dtool.engine.ResolvedModule;
import dtool.engine.SemanticManager;
import dtool.engine.operations.FindDefinitionResult.FindDefinitionResultEntry;

public class FindDefinitionOperation extends AbstractDToolOperation {
	
	public static final String FIND_DEF_PickedElementAlreadyADefinition = 
		"Element next to cursor is already a definition, not a reference.";
	public static final String FIND_DEF_NoReferenceFoundAtCursor = 
		"No reference found next to cursor.";
	public static final String FIND_DEF_MISSING_REFERENCE_AT_CURSOR = 
		FIND_DEF_NoReferenceFoundAtCursor;
	public static final String FIND_DEF_NoNameReferenceAtCursor = 
		"No name reference found next to cursor.";
	public static final String FIND_DEF_ReferenceResolveFailed = 
		"Definition not found for reference: ";
			
	public FindDefinitionOperation(SemanticManager semanticManager) {
		super(semanticManager);
	}
	
	public FindDefinitionResult findDefinition(Path filePath, final int offset) {
		if(filePath == null) {
			return new FindDefinitionResult("Invalid path for file: " );
		}
		final ResolvedModule resolvedModule;
		try {
			resolvedModule = getResolvedModule(filePath);
		} catch (ExecutionException e) {
			return new FindDefinitionResult("Error awaiting operation result: " + e);
		}
		final IModuleResolver mr = resolvedModule.getModuleResolver();
		Module module = resolvedModule.getModuleNode();
		
		assertEquals(module.compilationUnitPath, filePath); /*FIXME: BUG here normalization */
		return findDefinition(module, offset, mr);
	}
	
	public static FindDefinitionResult findDefinition(Module module, final int offset, final IModuleResolver mr) {
		
		ASTNodeFinder nodeFinder = new ASTNodeFinder(module, offset, true);
		
		if(nodeFinder.matchOnLeft instanceof NamedReference) {
			NamedReference namedReference = (NamedReference) nodeFinder.matchOnLeft;
			return doFindDefinition(namedReference, mr);
		} else if(nodeFinder.match instanceof Reference) {
			Reference reference = (Reference) nodeFinder.match;
			return doFindDefinition(reference, mr);
		} else if(nodeFinder.match instanceof DefSymbol){
			return new FindDefinitionResult(FIND_DEF_PickedElementAlreadyADefinition);
		}
		
		return new FindDefinitionResult(FIND_DEF_NoReferenceFoundAtCursor);
	}
	
	public static FindDefinitionResult doFindDefinition(Reference reference, final IModuleResolver mr) {
		if(reference instanceof NamedReference) {
			NamedReference namedReference = (NamedReference) reference;
			if(namedReference.isMissingCoreReference()) {
				return new FindDefinitionResult(FIND_DEF_MISSING_REFERENCE_AT_CURSOR, namedReference);
			} if(namedReference instanceof CommonQualifiedReference) {
				// Then the cursor is not actually next to an identifier.
				return new FindDefinitionResult(FIND_DEF_NoNameReferenceAtCursor);
			} else {
				return doFindDefinitionForRef(namedReference, mr);
			}
		} else {
			return new FindDefinitionResult(FIND_DEF_NoNameReferenceAtCursor);
		}
	}
	
	public static FindDefinitionResult doFindDefinitionForRef(Reference ref, IModuleResolver moduleResolver) {
		
		Collection<ILangNamedElement> namedElements = ref.findTargetDefElements(moduleResolver, false);
		
		if(namedElements == null || namedElements.size() == 0) {
			return new FindDefinitionResult(FIND_DEF_ReferenceResolveFailed + ref.toStringAsCode(), ref);
		}
		
		List<FindDefinitionResultEntry> results = new ArrayList<>();
		for (ILangNamedElement namedElement : namedElements) {
			final INamedElementNode node = namedElement.resolveUnderlyingNode();
			
			Path compilationUnitPath = null;
			SourceRange sourceRange = null;
			
			if(node != null) { // This can happen with intrinsic elements 
				compilationUnitPath = node.getModuleNode().getCompilationUnitPath();
				sourceRange = node.getNameSourceRangeOrNull();
			}
			
			results.add(new FindDefinitionResultEntry(
				namedElement.getExtendedName(),
				namedElement.isLanguageIntrinsic(), 
				compilationUnitPath,
				sourceRange));
		}
		
		return new FindDefinitionResult(results, ref, namedElements);
	}

}