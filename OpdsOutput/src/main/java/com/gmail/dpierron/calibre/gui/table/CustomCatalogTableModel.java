package com.gmail.dpierron.calibre.gui.table;

import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.Vector;

/**
 * The table model for the custom catalogs options table
 */
public class CustomCatalogTableModel implements TableModel {
  private static java.util.List<Composite<String, String>> customCatalogs;
  private static List<TableModelListener> listeners;

  public CustomCatalogTableModel() {
    if (customCatalogs == null) {
      customCatalogs = new Vector<Composite<String, String>>();
      listeners = new Vector<TableModelListener>();
    }
  }

  public void reset() {
    listeners = null;
    customCatalogs = null;
  }
  /**
   * Get the Custom catalogs String representation
   * (used to load the GUI control values)
   * TODO:  Look at whether a XML format could be used for the string (c2o-90)
   * @return
   */
  public List<Composite<String, String>> getCustomCatalogs() {
    return customCatalogs;
  }

  /**
   * Set the Custom Catalog values from their string representation
   * (used to store the GUI control values)
   * TODO:  Look at whether a XML format could be used for the string (c2o-90)
   * @param customCatalogs
   */
  public void setCustomCatalogs(List<Composite<String, String>> customCatalogs) {
    this.customCatalogs = customCatalogs;
  }

  /**
   * Add a new line to the Custom catalog table
   */
  public void addCustomCatalog() {
    customCatalogs.add(new Composite<String, String>("--title--", "--value--")); // add a new line
    fireTableChangedEvent(new TableModelEvent(this));
  }

  private void fireTableChangedEvent(TableModelEvent event) {
    for (TableModelListener listener : listeners) {
      listener.tableChanged(event);
    }
  }

  /**
   * Remove a line from the Custom catalog table
   * @param index
   */
  public void deleteCustomCatalog(int index) {
    if (index < customCatalogs.size()) {
      customCatalogs.remove(index);
      fireTableChangedEvent(new TableModelEvent(this));
    }
  }

  public int getRowCount() {
    return getCustomCatalogs().size();
  }

  public int getColumnCount() {
    return 3;
  }

  public String getColumnName(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Localization.Main.getText("config.CustomCatalogTitle.label");
      case 1:
        return Localization.Main.getText("config.CustomCatalogSavedSearchName.label");
      default:
        return "";
    }
  }

  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return true;
      case 1:
        return true;
      case 2:
        return true;
      default:
        return false;
    }
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return getCustomCatalogs().get(rowIndex).getFirstElement();
      case 1:
        return getCustomCatalogs().get(rowIndex).getSecondElement();
      case 2:
        return Localization.Main.getText("gui.delCustomCatalog");
      default:
        return "";
    }
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    Composite<String, String> val = getCustomCatalogs().get(rowIndex);
    switch (columnIndex) {
      case 0:
        getCustomCatalogs().set(rowIndex, new Composite<String, String>("" + aValue, val.getSecondElement()));
        break;
      case 1:
        getCustomCatalogs().set(rowIndex, new Composite<String, String>(val.getFirstElement(), "" + aValue));
        break;
    }
  }

  public void addTableModelListener(TableModelListener l) {
    if (!listeners.contains(l))
      listeners.add(l);
  }

  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }
}
