package com.gmail.dpierron.calibre.error;

/**
 * When the specified Calibre Saved search does not exist
*/
public class CalibreSavedSearchNotFoundException extends Exception {
  String savedSearchName;

  public CalibreSavedSearchNotFoundException(String savedSearchName) {
    super();
    this.savedSearchName = savedSearchName;
  }

  public CalibreSavedSearchNotFoundException(String savedSearchName, String message) {
    super(message);
    this.savedSearchName = savedSearchName;
  }

  public CalibreSavedSearchNotFoundException(String savedSearchName, String message, Throwable cause) {
    super(message, cause);
    this.savedSearchName = savedSearchName;
  }

  public String getSavedSearchName() {
    return savedSearchName;
  }

}
