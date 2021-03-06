/*******************************************************************************
 * Copyright (c) 2010, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.ast.definitions;

import static dtool.util.NewUtils.assertCast;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.nio.file.Path;

import melnorme.lang.tooling.ast.CommonASTNode;
import melnorme.lang.tooling.ast.IASTVisitor;
import melnorme.lang.tooling.ast.ILanguageElement;
import melnorme.lang.tooling.ast.IModuleNode;
import melnorme.lang.tooling.ast.SourceRange;
import melnorme.lang.tooling.ast.util.ASTCodePrinter;
import melnorme.lang.tooling.ast.util.NodeVector;
import melnorme.lang.tooling.ast_actual.ASTNode;
import melnorme.lang.tooling.ast_actual.ASTNodeTypes;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.resolver.NamedElementSemantics;
import melnorme.lang.tooling.engine.resolver.TypeSemantics;
import melnorme.lang.tooling.engine.scoping.CommonScopeLookup;
import melnorme.lang.tooling.engine.scoping.IScopeElement;
import melnorme.lang.tooling.engine.scoping.NamedElementsScope;
import melnorme.lang.tooling.engine.scoping.ScopeTraverser;
import melnorme.lang.tooling.symbols.IConcreteNamedElement;
import melnorme.lang.tooling.symbols.IImportableUnit;
import melnorme.lang.tooling.symbols.INamedElement;
import melnorme.lang.tooling.symbols.PackageNamespace;
import melnorme.utilbox.collections.ArrayView;
import dtool.ast.references.RefModule;
import dtool.parser.common.BaseLexElement;
import dtool.parser.common.IToken;
import dtool.parser.common.Token;
import dtool.util.NewUtils;

/**
 * D Module. 
 * The top-level AST class, has no parent, is the first and main node of every compilation unit.
 */
public class Module extends DefUnit implements IModuleNode, IConcreteNamedElement, IScopeElement, IImportableUnit {
	
	public static class ModuleDefSymbol extends DefSymbol {
		
		protected Module module;
		
		public ModuleDefSymbol(String id) {
			super(id);
		}
		
		@Override
		protected ASTNode getParent_Concrete() {
			return assertCast(parent, DeclarationModule.class);
		}
		
		@Override
		public DefUnit getDefUnit() {
			return module;
		}
		
		@Override
		protected CommonASTNode doCloneTree() {
			return new ModuleDefSymbol(name);
		}
		
	}
	
	public static class DeclarationModule extends ASTNode {
		
		public final Token[] comments;
		public final ArrayView<IToken> packageList;
		public final String[] packages; // Old API
		public final ModuleDefSymbol moduleName; 
		
		public DeclarationModule(Token[] comments, ArrayView<IToken> packageList, BaseLexElement moduleDefUnit) {
			this(comments, packageList, createModuleDefSymbol(moduleDefUnit));
		}
		
		public DeclarationModule(Token[] comments, ArrayView<IToken> packageList, ModuleDefSymbol moduleDefUnit) {
			this.comments = comments;
			this.packageList = assertNotNull(packageList);
			this.packages = RefModule.tokenArrayToStringArray(packageList);
			this.moduleName = parentize(moduleDefUnit);
		}
		
		protected static ModuleDefSymbol createModuleDefSymbol(BaseLexElement moduleDefUnit) {
			ModuleDefSymbol moduleName = new ModuleDefSymbol(moduleDefUnit.getSourceValue());
			moduleName.setSourceRange(moduleDefUnit.getSourceRange());
			moduleName.setParsedStatus();
			return moduleName;
		}
		
		public ModuleDefSymbol getModuleSymbol() {
			return (ModuleDefSymbol) moduleName;
		}
		
		@Override
		public ASTNodeTypes getNodeType() {
			return ASTNodeTypes.DECLARATION_MODULE;
		}
		
		@Override
		public void visitChildren(IASTVisitor visitor) {
			acceptVisitor(visitor, moduleName);
		}
		
		@Override
		protected CommonASTNode doCloneTree() {
			return new DeclarationModule(comments, packageList, clone(moduleName));
		}
		
		@Override
		public void toStringAsCode(ASTCodePrinter cp) {
			cp.append("module ");
			cp.appendTokenList(packageList, ".", true);
			cp.append(moduleName.name);
			cp.append(";");
		}
		
	}
	
	public static Module createModuleNoModuleDecl(String moduleName, NodeVector<ASTNode> members,
			Path compilationUnitPath, SourceRange modRange) {
		ModuleDefSymbol defSymbol = new ModuleDefSymbol(moduleName);
		defSymbol.setSourceRange(modRange.getStartPos(), 0);
		return new Module(defSymbol, null, members, compilationUnitPath);
	}
	
	public final DeclarationModule md;
	public final NodeVector<ASTNode> members;
	public final Path compilationUnitPath; // can be null. This might be removed in the future.
	
	public Module(ModuleDefSymbol defSymbol, DeclarationModule md, NodeVector<ASTNode> members, 
			Path compilationUnitPath) {
		super(defSymbol, false);
		defSymbol.module = this;
		this.md = parentize(md);
		this.members = parentize(members);
		assertNotNull(members);
		this.compilationUnitPath = compilationUnitPath;
		
		this.topLevelScope = createTopLevelScope();
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.MODULE;
	}
	
	@Override
	public void visitChildren(IASTVisitor visitor) {
		acceptVisitor(visitor, md);
		acceptVisitor(visitor, members);
	}
	
	@Override
	protected CommonASTNode doCloneTree() {
		return new Module((ModuleDefSymbol) clone(defName), clone(md), clone(members), compilationUnitPath);
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.append(md, cp.ST_SEP);
		cp.appendList(members, cp.ST_SEP);
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Module;
	}
	
	@Override
	public INamedElement getModuleElement() {
		return this;
	}
	
	@Override
	public String getModuleFullName() {
		return getFullyQualifiedName();
	}
	
	@Override
	public String getFullyQualifiedName() {
		ASTCodePrinter cp = new ASTCodePrinter();
		if(md != null) {
			cp.appendTokenList(md.packageList, ".", true);
		}
		cp.append(getName());
		return cp.toString();
	}
	
	public String[] getDeclaredPackages() {
		if(md != null) {
			return md.packages;
		}
		return NewUtils.EMPTY_STRING_ARRAY;
	}
	
	@Override
	public Token[] getDocComments() {
		if(md != null) {
			return md.comments;
		}
		return null;
	}
	
	@Override
	public Path getCompilationUnitPath() {
		return compilationUnitPath;
	}
	
	@Override
	public ILanguageElement getOwnerElement() {
		return this;
	}
	
	@Override
	public Path getSemanticContainerKey() {
		return getCompilationUnitPath();
	}
	
	/* -----------------  ----------------- */
	
	protected final NamedElementsScope topLevelScope;
	
	protected NamedElementsScope createTopLevelScope() {
		INamedElement topLevelElement = createTopLevelElement();
		return new NamedElementsScope(topLevelElement);
	}
	protected INamedElement createTopLevelElement() {
		if(md == null || md.packages.length == 0 || md.packages[0] == "") {
			return this;
		} else {
			String[] packNames = md.packages;
			return PackageNamespace.createNamespaceElement(packNames, this);
		}
	}
	
	/* -----------------  ----------------- */
	
	@Override
	public ScopeTraverser getScopeTraverser() {
		return new ScopeTraverser(members, true) {
			@Override
			public void evaluateSuperScopes(CommonScopeLookup lookup) {
				lookup.evaluateScope(topLevelScope);
				lookup.evaluateInMembersScope(
					CommonScopeLookup.resolveModule(lookup.context, Module.this, "object"));
			}
		};
	}
	
	@Override
	public IScopeElement getImportableScope() {
		return importableScope; // Note: we must return same instance of IScopeElement
	}
	
	protected final IScopeElement importableScope = new IScopeElement() {
		@Override
		public ScopeTraverser getScopeTraverser() {
			return new ScopeTraverser(members, true, true);
		}
	};
	
	@Override
	protected NamedElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		return new TypeSemantics(this, pickedElement, new MembersScopeElement(members));
	}
	
}