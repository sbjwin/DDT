package mmrnmhrm.ui.editor.codeassist;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;

import mmrnmhrm.ui.views.DeeElementImageProvider;

import org.dsource.ddt.ide.core.DeeNature;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;
import org.eclipse.swt.graphics.Image;

import dtool.ast.definitions.DefUnit;

public class DeeCompletionProposalCollector extends ScriptCompletionProposalCollector {
	
	protected final static char[] VAR_TRIGGER = { ' ', '=', ';', '.' };
	
	@Override
	protected String getNatureId() {
		return DeeNature.NATURE_ID;
	}
	
	@Override
	protected char[] getVarTrigger() {
		return VAR_TRIGGER;
	}
	
	public DeeCompletionProposalCollector(ISourceModule module) {
		super(module);
	}
	
	@Override
	public void accept(CompletionProposal proposal) {
		super.accept(proposal);
	}
	
	@Override
	protected IScriptCompletionProposal createScriptCompletionProposal(CompletionProposal proposal) {
		
		if(proposal.getExtraInfo() instanceof DefUnit) {
			DefUnit defUnit = (DefUnit) proposal.getExtraInfo();
			
			String completion = proposal.getCompletion();
			int replaceStart = proposal.getReplaceStart();
			int length = proposal.getReplaceEnd() - proposal.getReplaceStart() + 1; // BUG here
			Image image = DeeElementImageProvider.getNodeImage(defUnit);
			
			String displayString = defUnit.toStringForCodeCompletion();
			
			
			return new DeeCompletionProposal(completion, replaceStart, length, replaceStart + completion.length(),
					image, displayString, defUnit, null);
			
		} else {
			return super.createScriptCompletionProposal(proposal);
		}
	}
	
	// Specific proposals creation. May be use factory?
	@Override
	protected ScriptCompletionProposal createScriptCompletionProposal(String completion, int replaceStart, int length,
			Image image, String displayString, int i) {
		throw assertFail();
//		return new DeeCompletionProposal(completion, replaceStart, length, image, displayString, i);
	}
	
	@Override
	protected ScriptCompletionProposal createScriptCompletionProposal(String completion, int replaceStart, int length,
			Image image, String displayString, int i, boolean isInDoc) {
		throw assertFail();
//		return new DeeCompletionProposal(completion, replaceStart, length, image, displayString, i, isInDoc);
	}
	
	@Override
	protected ScriptCompletionProposal createOverrideCompletionProposal(
			IScriptProject scriptProject, ISourceModule compilationUnit,
			String name, String[] paramTypes, int start, int length,
			String displayName, String completionProposal) {
		throw assertFail();
//		return new ExamplePythonOverrideCompletionProposal(scriptProject, compilationUnit,
//				name, paramTypes, start, length, displayName,
//				completionProposal);
	}
	
}