/*******************************************************************************
 * Copyright (c) 2011, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.ui.editor.ref;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import static melnorme.utilbox.core.CoreUtil.downCast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import melnorme.lang.tooling.symbols.INamedElement;
import melnorme.utilbox.misc.ReflectionUtils;
import mmrnmhrm.ui.CommonDeeUITest;
import mmrnmhrm.ui.editor.AbstractLangEditor_DLTK;
import mmrnmhrm.ui.editor.codeassist.DeeCompletionProposal;

import org.eclipse.core.resources.IFile;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.templates.ScriptTemplateProposal;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;

public class ContentAssistUI_CommonTest extends CommonDeeUITest {
	
	protected final ISourceModule srcModule;
	protected final AbstractLangEditor_DLTK editor;
	
	public ContentAssistUI_CommonTest(IFile file) {
		this.editor = CommonDeeUITest.openDeeEditorForFile(file);
		this.srcModule = DLTKCore.createSourceModuleFrom(file);
	}
	
	public ContentAssistUI_CommonTest(ISourceModule sourceModule) {
		this.srcModule = sourceModule;
		this.editor = CommonDeeUITest.openDeeEditorForFile((IFile) sourceModule.getResource());
	}
	
	protected int getMarkerStartPos(String markerString) {
		String source = editor.getSourceViewer_().getDocument().get();
		int ccOffset = source.indexOf(markerString);
		assertTrue(ccOffset >= 0);
		return ccOffset;
	}
	
	protected int getMarkerEndPos(String markerString) {
		String source = editor.getSourceViewer_().getDocument().get();
		int ccOffset = source.indexOf(markerString);
		assertTrue(ccOffset >= 0);
		return ccOffset + markerString.length();
	}
	
	public static void invokeContentAssist(AbstractLangEditor_DLTK editor, int offset) {
		editor.getSourceViewer_().setSelectedRange(offset, 0);
		ITextOperationTarget target= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
		if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
			target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		}
	}
	
	public static ContentAssistant getContentAssistant(AbstractLangEditor_DLTK scriptEditor) {
		return (ContentAssistant) scriptEditor.getSourceViewer_().getContentAssistant();
	}
	
	protected static boolean isProposalPopupActive(ContentAssistant ca) {
		Method method = ReflectionUtils.getAvailableMethod(ca.getClass(), "isProposalPopupActive");
		return ReflectionUtils.uncheckedInvoke(ca, method);
	}
	
	
	protected static ICompletionProposal[] getProposals(ContentAssistant ca) throws NoSuchFieldException {
		// A bit of hack
		Object proposalPopup = ReflectionUtils.readField(ca, "fProposalPopup");
		return downCast(ReflectionUtils.readField(proposalPopup, "fFilteredProposals"));
	}
	
	/* ----------------------------------- */
	
	public static final int DONT_CHECK = -666;
	
	public static void checkProposals(ICompletionProposal[] proposals, int repOffset, int repLen, int prefixLen) {
		for(ICompletionProposal completionProposal : proposals) {
			if(completionProposal instanceof ScriptTemplateProposal) {
				continue;
			}
			DeeCompletionProposal proposal = (DeeCompletionProposal) completionProposal;
			String defName = proposal.namedElement.getExtendedName();
			
			assertTrue(repOffset == proposal.getReplacementOffset());
			assertTrue(repLen == proposal.getReplacementLength());
			if(prefixLen != DONT_CHECK) {
				String repStr = defName.substring(prefixLen);
				if(repStr.indexOf('(') != -1) {
					repStr = repStr.substring(0, repStr.indexOf('('));
				}
				assertTrue(repStr.equals(proposal.getReplacementString()));				
			}
		}
	}
	
	public static List<INamedElement> proposalsToDefUnitResults(ICompletionProposal[] proposals) {
		List<INamedElement> results = new ArrayList<>();
		for (ICompletionProposal iCompletionProposal : proposals) {
			if(iCompletionProposal instanceof DeeCompletionProposal) {
				DeeCompletionProposal deeCompletionProposal = (DeeCompletionProposal) iCompletionProposal;
				results.add(deeCompletionProposal.namedElement);
			}
		}
		return results;
	}
	
}