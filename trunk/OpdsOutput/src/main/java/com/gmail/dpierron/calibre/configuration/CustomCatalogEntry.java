package com.gmail.dpierron.calibre.configuration;

/**
 * Created by WalkerDJ on 24/04/2014.
 *
 * Class used with to describe a single value in the CustomCatalogs table
 */
public class CustomCatalogEntry {

  private String label;
  private String value;
  private Boolean atTop;

  public CustomCatalogEntry (String label, String value, Boolean atTop) {
    setLabel(label);
    setValue(value);
    setAtTop(atTop);
  }

  public String getLabel() { return label; }
  public String getValue() { return value;}
  public Boolean getAtTop() { return (atTop == null) ? false : atTop; }

  public void setLabel(String label) { this.label = label; }
  public void setValue(String value) { this.value = value; }
  public void setAtTop(Boolean atTop) {this.atTop = atTop; }

}
