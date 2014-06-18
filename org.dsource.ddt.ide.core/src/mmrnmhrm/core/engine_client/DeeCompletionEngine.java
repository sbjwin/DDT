package mmrnmhrm.core.engine_client;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import mmrnmhrm.core.DeeCoreMessages;

import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.CompletionContext;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.ModelException;

import dtool.ast.definitions.INamedElement;
import dtool.resolver.PrefixDefUnitSearch;
import dtool.resolver.api.ECompletionResultStatus;
import dtool.resolver.api.PrefixSearchOptions;

public class DeeCompletionEngine extends ScriptCompletionEngine {
	
	protected CompletionRequestor getRequestor() {
		return requestor;
	}
	
	@Override
	public void complete(IModuleSource moduleSource, final int position, int i) {
		assertNotNull(requestor);
		requestor.beginReporting();
		try {
			CompletionContext context = new CompletionContext();
			requestor.acceptContext(context);
			
			PrefixDefUnitSearch search = DToolClient.getDefault().doCodeCompletion(moduleSource, position);
			if(search.getResults().isEmpty() && search.getResultCode() != ECompletionResultStatus.RESULT_OK) {
				handleCompletionFailure(DeeCoreMessages.ContentAssist_LocationFailure, position);
			}
			
			for (INamedElement result : search.getResults()) {
				CompletionProposal proposal = createProposal(result, position, search.searchOptions);
				requestor.accept(proposal);				
			}
			
		} catch (ModelException e) {
			handleCompletionFailure(e.getMessage(), position);
		} finally {
			requestor.endReporting();
		}
	}
	
	protected void handleCompletionFailure(String errorMessage, final int position) {
		requestor.completionFailure(
			new DefaultProblem(errorMessage, null, null, ProblemSeverity.ERROR, position, position, 0));
	}
	
	protected CompletionProposal createProposal(INamedElement namedElem, int ccOffset,
		PrefixSearchOptions searchOptions) {
		String rplName;
		if(searchOptions.isImportModuleSearch) {
			rplName = namedElem.getFullyQualifiedName();
		} else {
			rplName = namedElem.getName();
		}
		
		String rplStr = rplName.substring(searchOptions.namePrefixLen);
		
		CompletionProposal proposal = new RefSearchCompletionProposal(ccOffset, searchOptions.isImportModuleSearch);
		proposal.setName(namedElem.getExtendedName());
		proposal.setCompletion(rplStr);
		proposal.setReplaceRange(ccOffset, ccOffset + searchOptions.rplLen);
		proposal.setExtraInfo(namedElem);
		
		return proposal;
	}
	
	public static class RefSearchCompletionProposal extends CompletionProposal {
		
		public final boolean isModuleImportCompletion;
		
		protected RefSearchCompletionProposal(int completionLocation, boolean isModuleImportCompletion) {
			super(CompletionProposal.TYPE_REF, completionLocation);
			this.isModuleImportCompletion = isModuleImportCompletion;
		}
		
		@Override
		public void setExtraInfo(Object extraInfo) {
			assertTrue(extraInfo instanceof INamedElement);
			super.setExtraInfo(extraInfo);
		}
		
		@Override
		public INamedElement getExtraInfo() {
			return (INamedElement) super.getExtraInfo();
		}
		
		public boolean isModuleImportCompletion() {
			return isModuleImportCompletion;
		}
		
	}
	
}