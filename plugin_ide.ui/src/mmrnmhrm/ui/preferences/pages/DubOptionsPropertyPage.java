/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.ui.preferences.pages;

import melnorme.lang.ide.ui.dialogs.AbstractProjectPropertyPage;
import mmrnmhrm.ui.preferences.DubProjectOptionsBlock;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class DubOptionsPropertyPage extends AbstractProjectPropertyPage {
	
	protected final DubProjectOptionsBlock prjBuildOptionsBlock = new DubProjectOptionsBlock();
	
	@Override
	protected Control createContents(Composite parent, IProject project) {
		prjBuildOptionsBlock.initializeFrom(getProject());
		return prjBuildOptionsBlock.createComponent(parent);
	}
	
	@Override
	public boolean performOk() {
		return prjBuildOptionsBlock.performOk();
	}
	
	@Override
	protected void performDefaults() {
		prjBuildOptionsBlock.restoreDefaults();
	}
	
}