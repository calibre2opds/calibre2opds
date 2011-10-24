package com.gmail.dpierron.calibre.error;

/**
 * When the specified Calibre search cannot be interpreted correctly (usually a syntax error)
*/
public class CalibreSavedSearchInterpretException extends Exception {
  String query;

  public CalibreSavedSearchInterpretException(String query, String message, Throwable cause) {
    super(message, cause);
    this.query = query;
  }

  public CalibreSavedSearchInterpretException(String query, Throwable cause) {
    super(cause);
    this.query = query;
  }

  public String getQuery() {
    return query;
  }
}
