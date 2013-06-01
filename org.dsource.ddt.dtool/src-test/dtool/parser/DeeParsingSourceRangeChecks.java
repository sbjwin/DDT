package dtool.parser;

import static dtool.util.NewUtils.assertNotNull_;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertEquals;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import dtool.ast.ASTNode;
import dtool.ast.ASTNodeTypes;
import dtool.ast.declarations.DeclarationAttrib;
import dtool.ast.declarations.DeclarationAttrib.AttribBodySyntax;
import dtool.ast.definitions.DefSymbol;
import dtool.ast.definitions.Module;
import dtool.ast.expressions.ExpLiteralMapArray.MapArrayLiteralKeyValue;
import dtool.ast.expressions.InitializerArray.ArrayInitEntry;
import dtool.ast.expressions.InitializerExp;
import dtool.ast.expressions.InitializerStruct.StructInitEntry;
import dtool.ast.expressions.MissingExpression;
import dtool.ast.expressions.MissingParenthesesExpression;
import dtool.ast.references.RefIdentifier;
import dtool.ast.references.Reference;
import dtool.ast.statements.CommonStatementList;
import dtool.ast.statements.ForeachRangeExpression;
import dtool.parser.DeeParser_RuleParameters.AmbiguousParameter;
import dtool.parser.DeeParser_RuleParameters.TplOrFnMode;

public class DeeParsingSourceRangeChecks {
	
	public static void runParsingSourceRangeChecks(ASTNode node, final String fullSource) {
		new DeeParsingSourceRangeChecks(fullSource, node).doCheck();
	}
	
	protected final String fullSource;
	protected final ASTNode nodeUnderTest;
	protected final String nodeSnippedSource;
	
	public DeeParsingSourceRangeChecks(String source, ASTNode node) {
		this.fullSource = assertNotNull_(source);
		this.nodeUnderTest = node;
		this.nodeSnippedSource = fullSource.substring(nodeUnderTest.getStartPos(), nodeUnderTest.getEndPos());
	}
	
	public void basicSourceRangeCheck() {
		
		LexElement firstLexElement = firstLexElementInSource(fullSource.substring(nodeUnderTest.getStartPos()));
		assertTrue(firstLexElement.precedingSubChannelTokens == null || canBeginWithEmptySpace(nodeUnderTest));
		
		if(nodeConsumesTrailingWhiteSpace(nodeUnderTest)) {
			// Check that the range contains all possible whitespace
			assertTrue(lexElementAfterSnippedRange(nodeUnderTest).getStartPos() == 0);
		}
	}
	
	public static LexElement firstLexElementInSource(String source) {
		return new LexElementProducer().produceLexElement(new DeeLexer(source));
	}
	
	public LexElement lexElementAfterSnippedRange(ASTNode node) {
		return firstLexElementInSource(fullSource.substring(node.getEndPos()));
	}
	
	public static boolean canBeginWithEmptySpace(final ASTNode node) {
		switch (node.getNodeType()) {
		case MODULE:
		case DECL_LIST:
		case SCOPED_STATEMENT_LIST:
		case CSTYLE_ROOT_REF:
		case MISSING_EXPRESSION:
			return true;

		case REF_IDENTIFIER:
		case REF_IMPORT_SELECTION:
			return DeeParser.isMissing((Reference) node);
		case INITIALIZER_EXP:
			return ((InitializerExp) node).exp instanceof MissingExpression;
		case STRUCT_INIT_ENTRY: {
			StructInitEntry initEntry = (StructInitEntry) node;
			return canBeginWithEmptySpace(initEntry.member != null ? initEntry.member : initEntry.value);
		}
		case ARRAY_INIT_ENTRY: {
			ArrayInitEntry initEntry = (ArrayInitEntry) node;
			return canBeginWithEmptySpace(initEntry.index != null ? initEntry.index : initEntry.value);
		}
		case MAPARRAY_ENTRY: {
			MapArrayLiteralKeyValue mapArrayEntry = (MapArrayLiteralKeyValue) node;
			return canBeginWithEmptySpace(mapArrayEntry.key);
		}
		case FOREACH_RANGE_EXPRESSION: {
			ForeachRangeExpression fre = (ForeachRangeExpression) node;
			return canBeginWithEmptySpace(fre.lower);
		}
		
		case BLOCK_STATEMENT:
		case BLOCK_STATEMENT_UNSCOPED: {
			return ((CommonStatementList) node).statements == null;
		}
		
		default:
			return false;
		}
	}
	
	
	public static boolean nodeConsumesTrailingWhiteSpace(final ASTNode node) {
		if(node instanceof DeclarationAttrib) {
			DeclarationAttrib declAttrib = (DeclarationAttrib) node;
			if(declAttrib.bodySyntax == AttribBodySyntax.COLON) {
				return true;
			}
		}
		if(node instanceof MissingExpression) {
			//return true; // TODO, require TypeOrExp parse changes
		}
		if(node instanceof RefIdentifier) {
			RefIdentifier refId = (RefIdentifier) node;
			return DeeParser.isMissing(refId); 
		}
		if(node instanceof InitializerExp) {
			InitializerExp initializerExp = (InitializerExp) node;
			return initializerExp.exp instanceof MissingExpression;
		}
		if(node instanceof DefSymbol) {
			return false;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void doCheck() {
		assertTrue(nodeUnderTest.getNodeType() != ASTNodeTypes.OTHER);
		
		basicSourceRangeCheck();
		
		
		switch (nodeUnderTest.getNodeType()) {
		
		case MODULE: {
			Module module = (Module) nodeUnderTest;
			int endPos = module.getEndPos();
			assertTrue(module.getStartPos() == 0 && (endPos == fullSource.length() || 
				fullSource.charAt(endPos) == 0x00 || fullSource.charAt(endPos) == 0x1A));
			return;
		}
		
		case MISSING_EXPRESSION:
			if(nodeUnderTest instanceof MissingParenthesesExpression) {
				SourceEquivalenceChecker.assertCheck(nodeUnderTest.toStringAsCode(), "");
			}
			
		default: 
			return;
		}
	}
	
	
	public static void checkNodeEquality(ASTNode reparsedNode, ASTNode node) {
		// We check the nodes are semantically equal by comparing the toStringAsCode
		// TODO: use a more accurate equals method?
		assertEquals(reparsedNode.toStringAsCode(), node.toStringAsCode());
	}
	
	protected void functionParamReparseCheck() {
		testParameter(true);
	}
	
	protected void templateParamReparseCheck() {
		testParameter(false);
	}
	
	protected void testParameter(boolean isFunction) {
		DeeParser snippedParser = prepParser(nodeSnippedSource);
		
		Object fromAmbig = new DeeParser_RuleParameters(snippedParser, TplOrFnMode.AMBIG).parseParameter();
		boolean isAmbig = false;
		if(fromAmbig instanceof AmbiguousParameter) {
			isAmbig = true;
			AmbiguousParameter ambiguousParameter = (AmbiguousParameter) fromAmbig;
			fromAmbig = isFunction ? ambiguousParameter.convertToFunction() : ambiguousParameter.convertToTemplate(); 
		}
		checkNodeEquality((ASTNode) fromAmbig, nodeUnderTest);
		snippedParser = prepParser(nodeSnippedSource);
		
		ASTNode paramParsedTheOtherWay = isFunction ? 
			snippedParser.parseTemplateParameter() : (ASTNode) snippedParser.parseFunctionParameter();
		
		boolean hasFullyParsedCorrectly = allSourceParsedCorrectly(snippedParser, paramParsedTheOtherWay);
		
		assertTrue(hasFullyParsedCorrectly ? isAmbig : true);
		if(hasFullyParsedCorrectly) {
			String expectedSource = nodeUnderTest.toStringAsCode();
			SourceEquivalenceChecker.assertCheck(paramParsedTheOtherWay.toStringAsCode(), expectedSource);
		}
	}
	
	public boolean allSourceParsedCorrectly(DeeParser parser, ASTNode resultNode) {
		return parser.lookAhead() == DeeTokens.EOF && resultNode.getData().hasErrors();
	}
	
	public static DeeParser prepParser(String snippedSource) {
		return new DeeParser(new DeeParsingChecks.DeeTestsLexer(snippedSource));
	}
	
}