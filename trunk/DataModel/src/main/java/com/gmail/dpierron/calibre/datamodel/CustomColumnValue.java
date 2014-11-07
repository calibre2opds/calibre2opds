package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.Arrays;
import java.util.List;
import java.lang.String;

/**
 * User: WalkerDJ
 *
 * Class that is used to store a custom column value
 */
public class CustomColumnValue {

  private CustomColumnType type;
  private String value;
  private String extra;

  //  CONSTRUCTORS

  public CustomColumnValue (CustomColumnType type, String value, String extra) {
    assert type != null;
    assert value != null;

    this.type = type;
    this.value = value;
    this.extra = extra;
  }

  // METHODS and PROPERTIES

  public CustomColumnType getType () {
    return type;
  }

  /**
   * Get Raw value as String
   *
   * This will convert values if any special treatment
   * is needed (e.g. Boolean or Series types)
   */
  public String getValueAsString() {
    assert Helper.isNotNullOrEmpty(value);
    String result = value;
    if (type.equals("bool")) {
        result = value.equals("0") ? "No" : "Yes";
          // TODO Localize yes/no values
//        result = value.equals("0") ? Localization.Main.getText("boolean.no")
//            : Localization.Main.getText("boolean.yes");
    } else if (type.equals("series")) {
      result += " [" + extra + "]";
    } else if (type.equals("float")) {
      // TODO  Decide if Calibre format string should be applied?
    } else if (type.equals("datetime")) {
      // TODO Decide if Calibre format string should be applied
    }
    return result;
  }

  public List<String> getValueAsList () {
    return Arrays.asList(getValueAsString());
  }

  /**
   * Get the value as a series if the column is of this type.
   * This is used to create a new series sub-section.
   * @return
   */
  public Series getValueAsSeries () {
    if (type.equals("series")) {
      Series series = new Series("0", value, extra);
      return series;
    } else {
      return null;
    }
  }
}

