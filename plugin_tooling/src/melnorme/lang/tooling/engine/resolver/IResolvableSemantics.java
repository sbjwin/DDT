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
package melnorme.lang.tooling.engine.resolver;

import java.util.Collection;

import melnorme.lang.tooling.bundles.IModuleResolver;
import melnorme.lang.tooling.bundles.ISemanticResolution;
import melnorme.lang.tooling.engine.INodeSemantics;
import melnorme.lang.tooling.symbols.INamedElement;

public interface IResolvableSemantics extends INodeSemantics {
	
	INamedElement findTargetDefElement(IModuleResolver moduleResolver);
	
	Collection<INamedElement> findTargetDefElements(IModuleResolver moduleResolver, boolean findOneOnly);
	
	ResolutionResult resolveTargetElement(ISemanticResolution sr);
	
	Collection<INamedElement> resolveTypeOfUnderlyingValue(IModuleResolver mr);
	
	/* ----------------- ----------------- */
	
	public static class NullResolvableSemantics extends AbstractResolvableSemantics {
		
		@Override
		public Collection<INamedElement> findTargetDefElements(IModuleResolver moduleResolver, boolean findOneOnly) {
			return null;
		}
		
	}
	
	public static final NullResolvableSemantics NULL_RESOLVABLE_SEMANTICS = new NullResolvableSemantics();

}