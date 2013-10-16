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

  public Long getId() {
    return id;
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
