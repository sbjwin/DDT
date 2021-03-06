package mmrnmhrm.org.eclipse.dltk.ui.actions;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import melnorme.lang.ide.ui.actions.UIUserInteractionsHelper;
import melnorme.lang.ide.ui.editor.EditorUtils;
import melnorme.lang.tooling.ast.ASTNodeFinder;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.context.ISemanticContext;
import melnorme.lang.tooling.context.ModuleFullName;
import melnorme.lang.tooling.symbols.INamedElement;
import mmrnmhrm.core.engine_client.DToolClient;
import mmrnmhrm.core.engine_client.DToolClient_Bad;
import mmrnmhrm.core.search.DeeDefPatternLocator;
import mmrnmhrm.core.search.SourceModuleFinder;
import mmrnmhrm.ui.actions.AbstractEditorOperationExt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.internal.ui.search.DLTKSearchQuery;
import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;
import org.eclipse.dltk.internal.ui.search.SearchMessages;
import org.eclipse.dltk.internal.ui.search.SearchUtil;
import org.eclipse.dltk.ui.actions.SelectionDispatchAction;
import org.eclipse.dltk.ui.search.PatternQuerySpecification;
import org.eclipse.dltk.ui.search.QuerySpecification;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import _org.eclipse.dltk.internal.ui.editor.ScriptEditor2;
import dtool.ast.definitions.DefSymbol;
import dtool.ast.definitions.Module;
import dtool.ast.references.Reference;
import dtool.engine.ModuleParseCache;
import dtool.parser.DeeParserResult.ParsedModule;

public abstract class FindAction extends SelectionDispatchAction {

	protected static final String SEARCH_REFS = "References";

	protected final ScriptEditor2 deeEditor;
	protected IWorkbenchSite fSite;

	public FindAction(ScriptEditor2 deeEditor) {
		super(deeEditor.getSite());
		this.deeEditor = deeEditor;
		this.fSite = deeEditor.getSite();
		init();
	}
	
	abstract void init();

	
	@Override
	public Shell getShell() {
		return fSite.getShell();
	}
	
	@Override
	public void run() {
		TextSelection sel = EditorUtils.getSelection(deeEditor);
		final int offset = sel.getOffset();
		
		new FindReferencesOperation(offset).executeAndHandle();
		
	}
	
	protected class FindReferencesOperation extends AbstractEditorOperationExt {
		
		protected final int offset;
		
		protected INamedElement defunit;
		protected String errorMessage;
		
		public FindReferencesOperation(int offset) {
			super(SEARCH_REFS, deeEditor);
			this.offset = offset;
		}
		
		@Override
		protected void performLongRunningComputation_do(IProgressMonitor monitor) throws CoreException {
			ModuleParseCache clientModuleCache = DToolClient.getDefault().getClientModuleCache();
			try {
				clientModuleCache.setWorkingCopyAndGetParsedModule(inputPath, doc.get());
				// This operation actually runs with the client cache, not on engine server
				performLongRunningComputation_withUpdatedServerWorkingCopy();
			} finally {
				if(sourceModule.isWorkingCopy() == false) {
					clientModuleCache.discardWorkingCopy(inputPath);
				}
			}
		}
		
		@Override
		protected void performLongRunningComputation_withUpdatedServerWorkingCopy() {
			ParsedModule parsedModule = DToolClient.getDefaultModuleCache().getParsedModuleOrNull(inputPath);
			if(parsedModule == null) {
				errorMessage = "Could not parse contents";
			}
			Module neoModule = parsedModule.module;
			ASTNode elem = ASTNodeFinder.findElement(neoModule, offset);
			if(elem instanceof DefSymbol) {
				DefSymbol defSymbol = (DefSymbol) elem;
				defunit = defSymbol.getDefUnit();
			} else if(elem instanceof Reference) {
				Reference ref = (Reference) elem;
				ISemanticContext mr = DToolClient_Bad.getResolverFor(inputPath);
				defunit = ref.resolveTargetElement(mr);
				if(defunit == null) {
					errorMessage = "No DefUnit found when resolving reference.";
				}
			} else {
				errorMessage = "Element is not a Definition nor a Reference";
			}
		}
		
		@Override
		protected void performOperation_handleResult() throws ModelException {
			if(errorMessage != null) {
				UIUserInteractionsHelper.openWarning(getShell(), SEARCH_REFS, errorMessage);
			}
			if(defunit != null) {
				startNewSearch(defunit);
			}
		}
		
	}
	
	protected void startNewSearch(INamedElement defunit) throws ModelException {
		assertNotNull(defunit);
		DLTKSearchQuery query= new DLTKSearchQuery(createQuery(defunit));
		if (query.canRunInBackground()) {
			/*
			 * This indirection with Object as parameter is needed to prevent the loading
			 * of the Search plug-in: the Interpreter verifies the method call and hence loads the
			 * types used in the method signature, eventually triggering the loading of
			 * a plug-in (in this case ISearchQuery results in Search plug-in being loaded).
			 */
			SearchUtil.runQueryInBackground(query);
		} else {
			IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
			/*
			 * This indirection with Object as parameter is needed to prevent the loading
			 * of the Search plug-in: the Interpreter verifies the method call and hence loads the
			 * types used in the method signature, eventually triggering the loading of
			 * a plug-in (in this case it would be ISearchQuery).
			 */
			IStatus status= NewSearchUI.runQueryInForeground(progressService, (ISearchQuery)query);
			if (status.matches(IStatus.ERROR | IStatus.INFO | IStatus.WARNING)) {
				ErrorDialog.openError(getShell(), SearchMessages.Search_Error_search_title, SearchMessages.Search_Error_search_message, status); 
			}
		}
	}

	protected QuerySpecification createQuery(INamedElement defunit) throws ModelException {
		DLTKSearchScopeFactory factory= DLTKSearchScopeFactory.getInstance();
		IDLTKSearchScope scope= factory.createWorkspaceScope(true, getLanguageToolkit());
		String description= factory.getWorkspaceScopeDescription(true);
		
		DeeDefPatternLocator.GLOBAL_param_defunit = defunit;
		return new PatternQuerySpecification(
				defunit.getName(), 0, true, getLimitTo(), scope, description);
		//return new ElementQuerySpecification(element, getLimitTo(), scope, description);
	}

	protected IDLTKLanguageToolkit getLanguageToolkit() {
		return deeEditor.getLanguageToolkit();
	}
	
	protected int getLimitTo() {
		return IDLTKSearchConstants.REFERENCES;
	}
	
	String getOperationUnavailableMessage() {
		return "This operation is not available for the selected element."; 
	}
	
	protected boolean isInsideInterpreterEnv(INamedElement defunit, DLTKSearchScopeFactory factory) throws ModelException {
		IScriptProject scriptProject = deeEditor.getInputModelElement().getScriptProject();
		
		boolean isInsideInterpreterEnvironment;
		String moduleFQName = defunit.getModuleFullName();
		if(moduleFQName == null) {
			isInsideInterpreterEnvironment = false;
		} else {
			ModuleFullName nameDescriptor = new ModuleFullName(moduleFQName);
			ISourceModule element = SourceModuleFinder.findModuleUnit(scriptProject, 
				nameDescriptor.getPackages(), nameDescriptor.getModuleSimpleName());
			// review this
			isInsideInterpreterEnvironment = element == null? false : factory.isInsideInterpreter(element);
		}
		return isInsideInterpreterEnvironment;
	}
	
}