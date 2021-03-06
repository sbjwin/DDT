/*******************************************************************************
 * Copyright (c) 2013, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.ui.views;

import static melnorme.utilbox.core.CoreUtil.tryCast;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.symbols.INamedElement;
import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.DefVarFragment;
import dtool.ast.definitions.DefinitionAggregate;
import dtool.ast.definitions.DefinitionAlias.DefinitionAliasFragment;
import dtool.ast.definitions.DefinitionAliasFunctionDecl;
import dtool.ast.definitions.DefinitionAliasVarDecl;
import dtool.ast.definitions.DefinitionAliasVarDecl.AliasVarDeclFragment;
import dtool.ast.definitions.DefinitionFunction;
import dtool.ast.definitions.DefinitionVariable;
import dtool.ast.definitions.FunctionParameter;
import dtool.ast.references.Reference;
import dtool.ddoc.TextUI;
import dtool.engine.analysis.IVarDefinitionLike;

public class DeeElementLabelProvider {
	
	public static String getLabelForContentAssistPopup(INamedElement namedElement) {
		
		switch (namedElement.getArcheType()) {
		case Module:
			return namedElement.getName();
		case Package:
			return namedElement.getName();
		default:
			break;
		}
		
		// We should NOT try to resolve namedElement to its true defUnit because that can be a costly operation,
		// and want to calculate a label quickly, without the need for parsing or other semantic operations
		DefUnit defUnit = tryCast(namedElement, DefUnit.class); 
		if(defUnit == null) {
			return namedElement.getName();
		}
		
		if(defUnit instanceof IVarDefinitionLike) {
//			IVarDefinitionLike varDefinitionLike = (IVarDefinitionLike) defUnit;
			// TODO: add more info to label, such as var type.
		}
		
		ASTCodePrinter cp = new ASTCodePrinter();
		
		switch (defUnit.getNodeType()) {
		case DEFINITION_VARIABLE: {
			DefinitionVariable elem = (DefinitionVariable) defUnit;
			return elem.getName() + getTypeSegment(elem.type) + getDefUnitContainerSuffix(defUnit);
		}
		case DEFINITION_VAR_FRAGMENT: {
			DefVarFragment elem = (DefVarFragment) defUnit;
			Reference type = elem.getDeclaredType();
			return elem.getName() + getTypeSegment(type) + getDefUnitContainerSuffix(defUnit);
		}
		
		case FUNCTION_PARAMETER: {
			FunctionParameter elem = (FunctionParameter) defUnit;
			return elem.getName() + getTypeSegment(elem.type) + getDefUnitContainerSuffix(defUnit);
		}
		
		case DEFINITION_FUNCTION: {
			DefinitionFunction elem = (DefinitionFunction) defUnit; 
			cp.append(elem.getName());
			cp.appendList("(", elem.tplParams, ",", ") ");
			cp.append(elem.toStringParametersForSignature());
			cp.append(getTypeSegment(elem.retType));
			cp.append(getDefUnitContainerSuffix(defUnit));
			return cp.toString();
		}
		
		case DEFINITION_ALIAS_FRAGMENT: {
			DefinitionAliasFragment elem = (DefinitionAliasFragment) defUnit;
			return elem.getName() + getAliasSegment(elem.target) + getDefUnitContainerSuffix(defUnit);
		}
		case DEFINITION_ALIAS_VAR_DECL: {
			DefinitionAliasVarDecl elem = (DefinitionAliasVarDecl) defUnit;
			return elem.getName() + getAliasSegment(elem.target) + getDefUnitContainerSuffix(defUnit);
		}
		case ALIAS_VAR_DECL_FRAGMENT: {
			AliasVarDeclFragment elem = (AliasVarDeclFragment) defUnit;
			return elem.getName() + getAliasSegment(elem.getAliasTarget()) + getDefUnitContainerSuffix(defUnit);
		}
		case DEFINITION_ALIAS_FUNCTION_DECL: {
			DefinitionAliasFunctionDecl elem = (DefinitionAliasFunctionDecl) defUnit;
			// TODO: print the correct alias target (a function type)
			return elem.getName() + getAliasSegment(elem.target) + "(?)" + getDefUnitContainerSuffix(defUnit);
		}
		
		
		default: break;
		}
		
		if(defUnit instanceof DefinitionAggregate) {
			return defUnit.getName() + getDefUnitContainerSuffix(defUnit);
		}
		
		return defUnit.getName() + getDefUnitContainerSuffix(defUnit);
	}

	public static String getTypeSegment(Reference typeRef) {
		return " : " + TextUI.typeRefToUIString(typeRef);
	}
	
	public static String getAliasSegment(Reference target) {
		String targetToString = target == null ? "?" : target.toStringAsCode();
		if(targetToString.isEmpty()) {
			targetToString = "?";
		}
		return " -> " + targetToString;
	}
	
	public static String getDefUnitContainerSuffix(DefUnit defUnit) {
		String moduleFullyQualifiedName = defUnit.getModuleFullName();
		return moduleFullyQualifiedName == null ? "" : " - " + moduleFullyQualifiedName;
	}
	
}