package com.gmail.dpierron.tools;

import java.io.File;
import java.io.IOException;

public class BaseOS extends OS {

  String osTypeName;
  
  public BaseOS(String osTypeName) {
    this.osTypeName = osTypeName;
  }
  
  @Override
  public void openFile(File file) throws IOException {
    try {
      Process process = _openFile(file);
      if (process != null) {
        try {
          process.waitFor();
        } catch (InterruptedException e) {
          // do nothing
        }
      }
      if (process == null || process.exitValue() != 0)
        throw new IOException();
    } catch (IOException e) {
      throw new IOException("Failed to open a file: "+file.getAbsolutePath()+" in OS: "+osTypeName);
    }
  }

  Process _openFile(File file) throws IOException {
    if (file == null) 
      throw new IOException("file is null");
    throw new IOException();
  }

}
