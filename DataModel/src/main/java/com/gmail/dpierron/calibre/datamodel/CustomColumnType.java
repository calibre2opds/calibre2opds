package com.gmail.dpierron.calibre.datamodel;

/**
 * Created with IntelliJ IDEA.
 * User: WalkerDJ
 * Class that is used to hold the type details of a custom column
 */
public class CustomColumnType {

  private long    id;
  private String  label;
  private String  name;
  private String  datatype;
  private String  display;

  //  CONSTRUCTORS

  public CustomColumnType(long id, String label, String name, String datatype, String display){
    this.id = id;
    this.label = label;
    this.name = name;
    this.datatype = datatype;
    this.display = display;
  }

  //  METHODS and PROPERTIES

  /**
   * Get Calibre internal Id for this custom column
   * @return
   */
  public Long getId() {
    return id;
  }

  /**
   * Get Calibre custom column name
   * @return
   */
  public String getLabel() {
    return label;
  }

  /**
   * Get Custom Column display name
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Get custom column type
   *
   * @return
   */
  public String getDatatype() {
    return datatype;
  }

  /**
   * Get custom column formatting string for displaying values
   *
   * @return
   */
  public String getDisplayFormat () {
    return display;
  }

  /**
   * check if a custom column with the given name actually exists!
   * @param name
   * @return
   */
  public boolean isCustomColumn (String name) {
    return false;
  }

}
