package mmrnmhrm.ui.editor;

import org.eclipse.dltk.internal.ui.editor.SourceModuleEditorActionContributor;
import org.eclipse.dltk.ui.actions.DLTKActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

import _org.eclipse.dltk.internal.ui.editor.ScriptEditor2;

public class DeeEditorActionContributor extends	SourceModuleEditorActionContributor {

	//private AbstractDeeEditorAction fGoToDefiniton;
	//private CommandContributionItem fGoToDefinitonHandler;
	//private AbstractDeeEditorAction ftestAction;
	
	/*
	public static CommandContributionItem getCommand_SearchReferences() {
		return new CommandContributionItem(new CommandContributionItemParameter(
				DeeUIPlugin.getActiveWorkbenchWindow(), null, 
				"GoToDefinitionHandler.COMMAND_ID", null,
				null, null, null, null, null, null,
				CommandContributionItem.STYLE_PUSH, null, true));
	}
	*/

	
	@Override
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			//navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fGoToDefiniton);
		}
	}


	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		//toolBarManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fGoToDefiniton);
		//toolBarManager.add(fGoToDefiniton);
		//toolBarManager.add(getCommand_FindDefinition());
	}
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		if (part instanceof ScriptEditor2) {
			getActionBars().setGlobalActionHandler(DLTKActionConstants.OPEN_TYPE_HIERARCHY, null);
			getActionBars().setGlobalActionHandler(DLTKActionConstants.OPEN_CALL_HIERARCHY, null);
		}
	}
	
}
