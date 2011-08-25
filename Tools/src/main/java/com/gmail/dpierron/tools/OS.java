package com.gmail.dpierron.tools;

import java.io.File;
import java.io.IOException;

public abstract class OS {
  public enum OsFamily {
    Windows,
    Linux,
    MacOsX
  }
  
  public enum OsType {
    
    WindowsXP("Windows XP", OsFamily.Windows),
    Windows7("Windows 7", OsFamily.Windows),
    Linux("Linux", OsFamily.Linux),
    MacOsX("Mac OS X", OsFamily.MacOsX);
    
    private String osTypeName;
    private OsFamily osFamily;
    
    private OsType(String osTypeName, OsFamily osFamily) {
      this.osTypeName = osTypeName;
      this.osFamily = osFamily;
    }
    
    public String getTypeName() {
      return osTypeName;
    }
    
    public OsFamily getFamily() {
      return osFamily;
    }
  }
  
  public abstract void openFile(File file) throws IOException;

  public static BaseOS factory() {
    return factory(System.getProperty("os.name"));
  }
  
  public static BaseOS factory(String osTypeName) {
    OsFamily family = null;
    for (OsType osType : OsType.values()) {
      if (osType.getTypeName().equalsIgnoreCase(osTypeName)) {
        family = osType.getFamily();
        break;
      }
    }

    if (family == null)
      return new BaseOS(osTypeName);

    switch (family) {
    case Windows:
      return new WindowsOS(osTypeName);
    case MacOsX:
      return new MacOS(osTypeName);
    case Linux:
      return new LinuxOS(osTypeName);
    default:
      return new BaseOS(osTypeName);
    }
  }
  
}
