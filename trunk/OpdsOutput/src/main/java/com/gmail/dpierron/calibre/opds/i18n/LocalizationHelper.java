package com.gmail.dpierron.calibre.opds.i18n;

import java.util.Vector;

import com.gmail.dpierron.tools.Helper;

public enum LocalizationHelper
{
  INSTANCE;
  
  private final static Object2StringConverter CONVERTER = new MethodCallerConverter("getHumanName");
  
  public Vector<String> getAvailableLocalizations() {
    return Localization.Main.getAvailableLocalizations();
  }
  
  public String getEnumConstantHumanName(Enum enumConstant) {
    return getEnumConstantHumanName(enumConstant, false);
  }
  
  public String getEnumConstantHumanName(Enum enumConstant, boolean secondary) {
    String label = uncheckedGetEnumConstantHumanName(enumConstant, secondary);
    // nothing worked, use the toString method
    if (label == null)
        label = enumConstant.toString();
    return label;
  }
  
  public String uncheckedGetEnumConstantHumanName(Enum enumConstant) {
    return uncheckedGetEnumConstantHumanName(enumConstant, false);
  }
  
  public String uncheckedGetEnumConstantHumanName(Enum enumConstant, boolean secondary) {
    if (enumConstant == null)
      return null;
    String label = null;
    
    if (!secondary) {
      // try calling a getHumanName method on the enum
      label = CONVERTER.getStringValueOrNull(enumConstant);
    }
    if (Helper.isNullOrEmpty(label)) {
      // try looking for a corresponding key in the Localization.Enum map
      String name = "enum."+enumConstant.getDeclaringClass().getCanonicalName()+"."+(secondary?"secondary.":"")+enumConstant.name();
      label = Localization.Enum.getText(name);
      if (label.equals(name))
        label = null;
    }
    return label;
  }

  public String getYesOrNo(Boolean b) {
    if (b == null) 
      return "";
    if (b.booleanValue())
      return Localization.Main.getText("boolean.yes");
    else
      return Localization.Main.getText("boolean.no");
  }
}
