package mmrnmhrm.core.model_elements;

import org.eclipse.dltk.core.IModelElement;

import dtool.ast.definitions.DefUnit;
import dtool.ast.definitions.EArcheType;

/**
 * A flag descriptor of a language element (usually a DefUnit) with enough info do determine an icon
 * The descriptor should consist of an int field only, 
 * so that it can be stored in {@link IModelElement}'s modifier flags.
 */
public class DefElementDescriptor {
	
	public int elementFlags;
	
	public DefElementDescriptor(int elementFlags) {
		this.elementFlags = elementFlags;
	}
	
	public DefElementDescriptor(DefUnit defUnit) {
		this.elementFlags = DefElementFlagsUtil.elementFlagsFromDefUnit(defUnit);
	}
	
	public EArcheType getArcheType() {
		return DefElementFlagsUtil.elementFlagsToArcheType(elementFlags);
	}
	
	public boolean isConstructor() {
		return getArcheType() == EArcheType.Constructor;
	}
	
	public boolean isNative() {
		return (elementFlags & DefElementFlagConstants.FLAG_NATIVE) != 0;
	}
	
	public boolean isOverride() {
		return (elementFlags & DefElementFlagConstants.FLAG_OVERRIDE) != 0;
	}
	
	public boolean isImmutable() {
		return (elementFlags & DefElementFlagConstants.FLAG_IMMUTABLE) != 0;
	}
	
	public boolean isConst() {
		return (elementFlags & DefElementFlagConstants.FLAG_CONST) != 0;
	}
	
	public boolean isFlag(int flag) {
		return (elementFlags & flag) != 0;
	}
	
	public void setArcheType(int flagKind) {
		elementFlags = elementFlags & ~DefElementFlagConstants.FLAGMASK_KIND; //Erase archetype
		elementFlags |= flagKind; // Add new archetype flags
	}
	
}