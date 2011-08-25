package com.gmail.dpierron.tools;

import java.io.File;
import java.io.IOException;

public class WindowsOS extends BaseOS {

  public WindowsOS(String osTypeName) {
    super(osTypeName);
  }

  @Override
  Process _openFile(File file) throws IOException {
    return Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"" + file.getAbsolutePath()+"\"");
  }

}
