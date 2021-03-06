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
package mmrnmhrm.core.build;

import melnorme.lang.ide.core.tests.CommonCoreTest;
import mmrnmhrm.core.DeeCore;
import mmrnmhrm.tests.SampleProject;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public class DubProjectBuilderTest extends CommonCoreTest {
	
	protected static SampleProject sampleProj;
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		try(SampleProject project = new SampleProject(getClass().getSimpleName())) {
			sampleProj = project;
			
			testBuilder();
			
			// Await buildpath update, to prevent logging of error.
			DeeCore.getWorkspaceModelManager().syncPendingUpdates();
		}
	}
	
	protected void testBuilder() throws CoreException {
		sampleProj.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		sampleProj.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		sampleProj.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		sampleProj.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}
	
}