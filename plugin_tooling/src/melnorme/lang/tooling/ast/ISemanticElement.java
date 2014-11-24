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
package melnorme.lang.tooling.ast;

import java.nio.file.Path;

public interface ISemanticElement {
	
	/** 
	 * @return true if this is a pre-defined/native language element. 
	 * (example: primitives such as int, void, or native types like arrays, pointer types).
	 * This is a special case for which the elements do not have a well defined containing module path. 
	 */
	public abstract boolean isLanguageIntrinsic();
	
	public abstract Path getModulePath();
	
}