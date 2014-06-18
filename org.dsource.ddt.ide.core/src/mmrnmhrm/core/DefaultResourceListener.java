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
package mmrnmhrm.core;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

public class DefaultResourceListener implements IResourceChangeListener {
	
	@Override
	public void resourceChanged(IResourceChangeEvent resourceChange) {
		IResourceDelta workspaceDelta = resourceChange.getDelta();
		assertTrue(workspaceDelta != null);
		
		for (IResourceDelta projectDelta : workspaceDelta.getAffectedChildren()) {
			processProjectDelta(projectDelta);
		}
	}
	
	//@SuppressWarnings("unused")
	protected void processProjectDelta(IResourceDelta projectDelta) {
		assertTrue(projectDelta.getResource().getType() == IResource.PROJECT);
	}
	
}