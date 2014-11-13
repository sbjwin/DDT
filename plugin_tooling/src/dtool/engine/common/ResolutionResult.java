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
package dtool.engine.common;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.List;

import melnorme.lang.tooling.ast_actual.ILangNamedElement;
import melnorme.utilbox.collections.ArrayList2;

public class ResolutionResult {
	
	protected final List<ILangNamedElement> results;
	
	public ResolutionResult(ILangNamedElement... results) {
		this.results = new ArrayList2<>(results);
	}
	
	public ILangNamedElement getSingleResult() {
		assertTrue(results.size() <= 1);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
}