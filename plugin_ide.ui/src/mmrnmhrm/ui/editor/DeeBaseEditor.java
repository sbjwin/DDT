package mmrnmhrm.ui.editor;

import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;

import _org.eclipse.dltk.internal.ui.editor.BracketInserter2;

public abstract class DeeBaseEditor extends AbstractLangEditor_DLTK {
	
	protected BracketInserter2 fBracketInserter = new DeeBracketInserter(this);
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		boolean closeBrackets = true;
		boolean closeStrings = true;
		boolean closeAngularBrackets = false;
		
		fBracketInserter.setCloseBracketsEnabled(closeBrackets);
		fBracketInserter.setCloseStringsEnabled(closeStrings);
		fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);
		
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension) {
			((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(fBracketInserter);
		}
	}
	
	@Override
	public void dispose() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension) {
			((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(fBracketInserter);
		}
		super.dispose();
	}
	
	@Override
	protected void doSelectionChanged(SelectionChangedEvent event) {
		// This is the normal path
		super.doSelectionChanged(event);
	}
	
}