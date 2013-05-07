package dtool.ast.declarations;

import java.util.Iterator;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTNode;
import dtool.ast.ASTCodePrinter;
import dtool.ast.ASTNode;
import dtool.ast.ASTNodeTypes;
import dtool.ast.IASTVisitor;
import dtool.ast.ISourceRepresentation;
import dtool.ast.definitions.Definition;
import dtool.refmodel.INonScopedBlock;

public class DeclarationProtection extends DeclarationAttrib {
	
	public enum Protection implements ISourceRepresentation {
	    PRIVATE,
	    PACKAGE,
	    PROTECTED,
	    PUBLIC,
	    EXPORT,
	    ;
	    
		@Override
		public String getSourceValue() {
			return toString().toLowerCase();
		}
	}
	
	public final Protection protection;
	
	public DeclarationProtection(Protection protection, AttribBodySyntax bodySyntax, ASTNode bodyDecls) {
		super(bodySyntax, bodyDecls);
		this.protection = protection;
		
		localAnalysis();
	}
	
	@Override
	public ASTNodeTypes getNodeType() {
		return ASTNodeTypes.DECLARATION_PROTECTION;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, body);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public void toStringAsCode(ASTCodePrinter cp) {
		cp.appendToken(protection);
		cp.append(" ");
		toStringAsCode_body(cp);
	}
	
	public void localAnalysis() {
		applyAttributes(this);
	}
	
	protected void applyAttributes(INonScopedBlock block) {
		Iterator<? extends IASTNode> iter = block.getMembersIterator();
		while(iter.hasNext()) {
			IASTNode node = iter.next();
			
			if(node instanceof Definition) {
				Definition def = (Definition) node;
				def.setProtection(protection);
			} else if (node instanceof DeclarationProtection) {
				// Do not descend, that inner decl take priority
			} else if (node instanceof DeclarationImport && protection == Protection.PUBLIC) {
				DeclarationImport declImport = (DeclarationImport) node;
				declImport.isTransitive = true;
			} else if(node instanceof INonScopedBlock) {
				applyAttributes((INonScopedBlock) node);
			}
		}
	}
	
}