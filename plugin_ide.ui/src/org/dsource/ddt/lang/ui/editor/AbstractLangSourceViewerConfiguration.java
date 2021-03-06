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
package org.dsource.ddt.lang.ui.editor;


import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import melnorme.lang.ide.ui.TextSettings_Actual;
import melnorme.lang.ide.ui.text.coloring.AbstractLangScanner;
import melnorme.lang.ide.ui.text.coloring.SingleTokenScanner;

import org.eclipse.cdt.internal.ui.text.TokenStore;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.ui.text.ITokenStore;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Abstract SourceViewConfiguration
 * Has code to help manage the configured scanners, and let respond to preference changes.
 */
public abstract class AbstractLangSourceViewerConfiguration extends ScriptSourceViewerConfiguration {
	
	protected final IPreferenceStore preferenceStore;
	protected final IColorManager colorManager;
	protected Map<String, AbstractLangScanner> scannersByContentType;
	
	public AbstractLangSourceViewerConfiguration(IPreferenceStore preferenceStore, 
			final IColorManager colorManager, ITextEditor editor, String partitioning) {
		super(new DLTKColorManager(colorManager), assertNotNull(preferenceStore), editor, partitioning);
		this.preferenceStore = preferenceStore;
		this.colorManager = colorManager;
		scannersByContentType = new HashMap<>();
		createScanners();
		scannersByContentType = Collections.unmodifiableMap(scannersByContentType);
	}
	
	protected org.eclipse.cdt.ui.text.IColorManager getColorManager2() {
		return colorManager;
	}
	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return TextSettings_Actual.PARTITION_TYPES;
	}
	
	protected abstract void createScanners();
	
	protected void addScanner(AbstractLangScanner scanner, String... contentTypes) {
		for (String contentType : contentTypes) {
			scannersByContentType.put(contentType, scanner);
		}
	}
	
	protected SingleTokenScanner createSingleTokenScanner(String tokenProperty) {
		return new SingleTokenScanner(getTokenStoreFactory(), tokenProperty);
	}
	
	protected ITokenStoreFactory getTokenStoreFactory() {
		return new ITokenStoreFactory() {
			@Override
			public ITokenStore createTokenStore(String[] propertyColorNames) {
				return new TokenStore(getColorManager2(), fPreferenceStore, propertyColorNames);
			}
		};
	}
	
	public Collection<AbstractLangScanner> getScanners() {
		return scannersByContentType.values();
	}
	
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = createPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		setupPresentationReconciler(reconciler);
		return reconciler;
	}
	
	protected PresentationReconciler createPresentationReconciler() {
		return new PresentationReconciler();
	}
	
	protected void setupPresentationReconciler(PresentationReconciler reconciler) {
		for (Entry<String, AbstractLangScanner> entry : scannersByContentType.entrySet()) {
			String contentType = entry.getKey();
			AbstractLangScanner scanner = entry.getValue();
			DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
			reconciler.setDamager(dr, contentType);
			reconciler.setRepairer(dr, contentType);
		}
	}
	
	@Override
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		for (AbstractLangScanner scanner : getScanners()) {
			if(scanner.affectsBehavior(event))
				return true;
		}
		return false;
	}
	
	@Override
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		for (AbstractLangScanner scanner : getScanners()) {
			if (scanner.affectsBehavior(event)) {
				scanner.adaptToPreferenceChange(event);
			}
		}
	}
	
	public void setupViewerForTextPresentationPrefChanges(final SourceViewer viewer) {
		final IPreferenceStore preferenceStore = getPreferenceStore();
		
		final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (affectsTextPresentation(event)) {
					handlePropertyChangeEvent(event);
					viewer.invalidateTextPresentation();
				}
			}
		};
		viewer.getTextWidget().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				preferenceStore.removePropertyChangeListener(propertyChangeListener);
			}
		});
		
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}
	
}