package com.gmail.dpierron.tools;

import java.io.File;
import java.io.IOException;

public class MacOS extends BaseOS {

  public MacOS(String osTypeName) {
    super(osTypeName);
  }

  @Override
  Process _openFile(File file) throws IOException {
    return Runtime.getRuntime().exec("open " + file.getAbsolutePath());
  }

}
