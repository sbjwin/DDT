package mmrnmhrm.ui.editor.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import mmrnmhrm.ui.DeePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JDT_PreferenceConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Bundle;

import descent.core.ddoc.Ddoc;
import dtool.ast.ASTCodePrinter;
import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.DefVarFragment;
import dtool.ast.definitions.DefinitionAggregate;
import dtool.ast.definitions.DefinitionFunction;
import dtool.ast.definitions.DefinitionVariable;
import dtool.ast.definitions.Module;
import dtool.ast.references.RefModule.LiteModuleDummy;
import dtool.ddoc.DeeDocAccessor;
import dtool.ddoc.IDeeDocColorConstants;

public class HoverUtil {
	
	public static class DeePluginPreferences {
		public static String getPreference(String key, @SuppressWarnings("unused") IProject project) {
			return DeePlugin.getPrefStore().getString(key);
		}
	}

	private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

	/** Gets the HTML info for the given DefUnit. */
	public static String getDefUnitHoverInfoWithDeeDoc(DefUnit defUnit) {
		String sig = getLabelForHoverSignature(defUnit);
		String str = convertToHTMLContent(sig);
		str = "<b>" +str+ "</b>" 
		+"  <span style=\"color: #915F6D;\" >"+
			"("+defUnit.getArcheType().toString()+")"+"</span>";

		
		Ddoc ddoc = DeeDocAccessor.getDdoc(defUnit);
		if(ddoc != null) {
			StringBuffer stringBuffer = DeeDocAccessor.transform(ddoc, EMPTY_MAP);
			str += "<br/><br/>" + stringBuffer.toString();
		}
		return str;
	}
	
	public static String getLabelForHoverSignature(DefUnit defUnit) {
		ASTCodePrinter cp = new ASTCodePrinter();
		
		if(defUnit instanceof LiteModuleDummy) {
			LiteModuleDummy module = (LiteModuleDummy) defUnit;
			return module.getFullyQualifiedName();
		}
		
		switch (defUnit.getNodeType()) {
		case MODULE: {
			Module module = (Module) defUnit;
			return module.getFullyQualifiedName();
		}
		case DEFINITION_VARIABLE: {
			DefinitionVariable var = (DefinitionVariable) defUnit;
			
			String str = var.getTypeString() + " " + var.getName();
			return str;
		}
		case DEFINITION_VAR_FRAGMENT: {
			DefVarFragment fragment = (DefVarFragment) defUnit;
			
			String str = fragment.getTypeString() + " " + fragment.getName();
			return str;
		}

		case DEFINITION_FUNCTION: {
			DefinitionFunction function = (DefinitionFunction) defUnit; 
			cp.appendStrings(function.typeRefToUIString(function.retType), " ");
			cp.append(function.getName());
			cp.appendList("(", function.tplParams, ",", ") ");
			cp.appendList("(", function.getParams_asNodes(), ",", ") ");
			return cp.toString();
		}
		
		default:
			break;
		}
		
		if(defUnit instanceof DefinitionAggregate) {
			DefinitionAggregate defAggr = (DefinitionAggregate) defUnit;
			cp.append(defAggr.getName());
			cp.appendList("(", defAggr.tplParams, ",", ") ");
			return cp.toString();
		}
		
		// Default hover signature:
		return defUnit.getName();
	}

	@SuppressWarnings("restriction")
	private static String convertToHTMLContent(String str) {
		
		str = org.eclipse.jface.internal.text.html.
			HTMLPrinter.convertToHTMLContent(str);
		str = str.replace("\n", "<br/>");
		return str;
	}

	public static String loadStyleSheet(String cssfilepath) {
		Bundle bundle= Platform.getBundle(JavaPlugin.getPluginId());
		URL url= bundle.getEntry(cssfilepath); //$NON-NLS-1$
		if (url != null) {
			try {
				url= FileLocator.toFileURL(url);
				BufferedReader reader= new BufferedReader(new InputStreamReader(url.openStream()));
				StringBuffer buffer= new StringBuffer(200);
				String line= reader.readLine();
				while (line != null) {
					buffer.append(line);
					buffer.append('\n');
					line= reader.readLine();
				}
				return buffer.toString();
			} catch (IOException ex) {
				JavaPlugin.log(ex);
			}
		}
		return null;
	}

	@SuppressWarnings("restriction")
	public static String getCompleteHoverInfo(String info, String cssStyle) {
		
		if (info != null && info.length() > 0) {
			StringBuffer buffer= new StringBuffer();
			org.eclipse.jface.internal.text.html.
			HTMLPrinter.insertPageProlog(buffer, 0, cssStyle);
			buffer.append(info);
			org.eclipse.jface.internal.text.html.
			HTMLPrinter.addPageEpilog(buffer);
			info= buffer.toString();
		}
		return info;
	}

	@SuppressWarnings("restriction")
	static String setupCSSFont(String fgCSSStyles) {
		String css= fgCSSStyles;
		if (css != null) {
			FontData fontData= JFaceResources.getFontRegistry().
				getFontData(JDT_PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css= org.eclipse.jface.internal.text.html.
				HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}
	
	public static final String CODE_CSS_CLASS 
	= ".code		 { font-family: monospace; background-color: #e7e7e8; border: 2px solid #cccccc; padding: 0ex;}";

	
	public static String getDDocPreparedCSS(String filename) {
		String str = HoverUtil.loadStyleSheet(filename);
		str = HoverUtil.setupCSSFont(str);
		StringBuffer strBuf = new StringBuffer(str);
		strBuf.append(CODE_CSS_CLASS);
		addPreferencesFontsAndColorsToStyleSheet(strBuf);
		return strBuf.toString();
	}
	
	static void addPreferencesFontsAndColorsToStyleSheet(StringBuffer buffer) {
		addStyle(buffer, IDeeDocColorConstants.JAVA_KEYWORD);
		addStyle(buffer, IDeeDocColorConstants.JAVA_KEYWORD_RETURN);
		addStyle(buffer, IDeeDocColorConstants.JAVA_SPECIAL_TOKEN);
		addStyle(buffer, IDeeDocColorConstants.JAVA_OPERATOR);
		addStyle(buffer, IDeeDocColorConstants.JAVA_DEFAULT);
		addStyle(buffer, IDeeDocColorConstants.JAVA_PRAGMA);
		addStyle(buffer, IDeeDocColorConstants.JAVA_STRING);
		addStyle(buffer, IDeeDocColorConstants.JAVA_SINGLE_LINE_COMMENT);
		addStyle(buffer, IDeeDocColorConstants.JAVA_SINGLE_LINE_DOC_COMMENT);
		addStyle(buffer, IDeeDocColorConstants.JAVA_MULTI_LINE_COMMENT);
		addStyle(buffer, IDeeDocColorConstants.JAVA_MULTI_LINE_PLUS_COMMENT);
		addStyle(buffer, IDeeDocColorConstants.JAVA_MULTI_LINE_PLUS_DOC_COMMENT);
		addStyle(buffer, IDeeDocColorConstants.JAVADOC_DEFAULT);
	}
	
	private static void addStyle(StringBuffer buffer, String partialPreferenceKey) {
		//IJavaProject javaProject = null;
		
		buffer.append("."); //$NON-NLS-1$
		buffer.append(partialPreferenceKey);
		buffer.append("{"); //$NON-NLS-1$

		String colorString = DeePluginPreferences.getPreference(partialPreferenceKey, null);
		RGB color = colorString == null ? new RGB(0, 0, 0) : StringConverter.asRGB(colorString);
		buffer.append("color: "); //$NON-NLS-1$
		HTMLPrinter_appendColor(buffer, color);
		buffer.append(";"); //$NON-NLS-1$
		
		String boolString;
		boolean bool, bool2;
		
		boolString = DeePluginPreferences.getPreference(partialPreferenceKey + PreferenceConstants.EDITOR_BOLD_SUFFIX, null);
		bool = convertToBool(boolString);
		if (bool) {
			buffer.append("font-weight: bold;"); //$NON-NLS-1$
		}
		
		boolString = DeePluginPreferences.getPreference(partialPreferenceKey + PreferenceConstants.EDITOR_ITALIC_SUFFIX, null);
		bool = convertToBool(boolString);
		if (bool) {
			buffer.append("font-style: italic;"); //$NON-NLS-1$
		}
		
		boolString = DeePluginPreferences.getPreference(partialPreferenceKey + PreferenceConstants.EDITOR_UNDERLINE_SUFFIX, null);
		bool = convertToBool(boolString);
		
		boolString = DeePluginPreferences.getPreference(partialPreferenceKey + PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX, null);
		bool2 = convertToBool(boolString);
		
		if (bool || bool2) {
			buffer.append("text-decoration:"); //$NON-NLS-1$
			if (bool) {
				buffer.append("underline"); //$NON-NLS-1$
			}
			if (bool && bool2) {
				buffer.append(", "); //$NON-NLS-1$
			}
			if (bool2) {
				buffer.append("line-through"); //$NON-NLS-1$
			}
			buffer.append(";"); //$NON-NLS-1$
		}
		
		buffer.append("}\n"); //$NON-NLS-1$
	}

	private static boolean convertToBool(String boolString) {
		return boolString == null || boolString.equals("") ? 
				false : StringConverter.asBoolean(boolString);
	}

	
	public static void HTMLPrinter_appendColor(StringBuffer buffer, RGB rgb) {
		buffer.append('#');
		buffer.append(toHexString(rgb.red));
		buffer.append(toHexString(rgb.green));
		buffer.append(toHexString(rgb.blue));
	}
	
	private static String toHexString(int value) {
		String s = Integer.toHexString(value);
		if (s.length() != 2) {
			return "0" + s;
		}
		return s;
	}

}
