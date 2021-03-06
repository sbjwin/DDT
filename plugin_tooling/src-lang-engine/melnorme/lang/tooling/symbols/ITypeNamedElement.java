/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.symbols;

import melnorme.lang.tooling.context.ISemanticContext;
import melnorme.lang.tooling.engine.resolver.TypeSemantics;


/**
 * Marker interface for non-alias INamedElements that can be used to declare variables.
 */
public interface ITypeNamedElement extends IConcreteNamedElement {
	
	@Override
	public TypeSemantics getSemantics(ISemanticContext parentContext);
	
}