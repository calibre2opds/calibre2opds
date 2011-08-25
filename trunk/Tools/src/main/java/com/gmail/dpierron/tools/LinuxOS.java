package com.gmail.dpierron.tools;

import java.io.File;
import java.io.IOException;

public class LinuxOS extends BaseOS {

  public LinuxOS(String osTypeName) {
    super(osTypeName);
  }

  @Override
  Process _openFile(File file) throws IOException {
    Process result = null;
    try {
      result = Runtime.getRuntime().exec("konqueror file:///" + file.getAbsolutePath());
    } catch (Exception e) {
      result = Runtime.getRuntime().exec("gnome-open " + file.getAbsolutePath());
    }
    return result;
  }

}
