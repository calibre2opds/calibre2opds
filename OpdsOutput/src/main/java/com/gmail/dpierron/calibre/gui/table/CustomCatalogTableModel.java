package com.gmail.dpierron.calibre.gui.table;

import com.gmail.dpierron.calibre.configuration.CustomCatalogEntry;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.tools.i18n.Localization;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.Vector;

/**
 * The table model for the custom catalogs options table
 *  The <code>TableModel</code> interface specifies the methods the
 *  <code>JTable</code> will use to interrogate a tabular data model. <p>
 *
 *  The <code>JTable</code> can be set up to display any data
 *  model which implements the
 *  <code>TableModel</code> interface with a couple of lines of code:  <p>
 *  <pre>
 *      TableModel myData = new MyTableModel();
 *      JTable table = new JTable(myData);
 *  </pre><p>
 *
 * For further documentation, see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#data">Creating a Table Model</a>
 * in <em>The Java Tutorial</em>.
 * <p>
 * @author Philip Milne
 * @see javax.swing.JTable
 */

public class CustomCatalogTableModel implements TableModel {
  private static List<CustomCatalogEntry> customCatalogs;
  private static List<TableModelListener> listeners;

  public CustomCatalogTableModel() {
    if (customCatalogs == null) {
      customCatalogs = new Vector<CustomCatalogEntry>();
      listeners = new Vector<TableModelListener>();
    }
  }

  /**
   * Get the Custom catalogs String representation
   * (used to load the GUI control values)
   * TODO:  Look at whether a XML format could be used for the string (c2o-90)
   * @return
   */
  public List<CustomCatalogEntry> getCustomCatalogs() {
    return customCatalogs;
  }

  /**
   * Set the Custom Catalog values from their string representation
   * (used to store the GUI control values)
   * TODO:  Look at whether a XML format could be used for the string (c2o-90)
   * @param customCatalogs
   */
  public void setCustomCatalogs(List<CustomCatalogEntry> customCatalogs) {
    this.customCatalogs = customCatalogs;
    fireTableChangedEvent(new TableModelEvent(this));
  }

  /**
   * Add a new line to the Custom catalog table
   */
  public void addCustomCatalog() {
    customCatalogs.add(new CustomCatalogEntry(Constants.CUSTOMCATALOG_DEFAULT_TITLE, Constants.CUSTOMCATALOG_DEFAULT_SEARCH, false)); // add a new line
    fireTableChangedEvent(new TableModelEvent(this));
  }

  private void fireTableChangedEvent(TableModelEvent event) {
    assert listeners != null && listeners.size() > 0;
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

  /**
   * Returns the number of rows in the model. A
   * <code>JTable</code> uses this method to determine how many rows it
   * should display.  This method should be quick, as it
   * is called frequently during rendering.
   *
   * @return the number of rows in the model
   * @see #getColumnCount
   */
  public int getRowCount() {
    return getCustomCatalogs().size();
  }

  /**
   * Returns the number of columns in the model. A
   * <code>JTable</code> uses this method to determine how many columns it
   * should create and display by default.
   *
   * @return the number of columns in the model
   * @see #getRowCount
   */
  public int getColumnCount() {
    return 4;
  }

  /**
   * Returns the name of the column at <code>columnIndex</code>.  This is used
   * to initialize the table's column header name.  Note: this name does
   * not need to be unique; two columns in a table can have the same name.
   *
   * @param   columnIndex     the index of the column
   * @return  the name of the column
   */
  public String getColumnName(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Localization.Main.getText("config.CustomCatalogTitle.label");
      case 1:
        return Localization.Main.getText("config.CustomCatalogSavedSearchName.label");
      case 2:
        return Localization.Main.getText("config.CustomCatalogSavedSearchTop.label");
      default:
        return "";
    }
  }

  /**
   * Returns the most specific superclass for all the cell values
   * in the column.  This is used by the <code>JTable</code> to set up a
   * default renderer and editor for the column.
   *
   * @param columnIndex  the index of the column
   * @return the common ancestor class of the object values in the model.
   */
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
     case 0:                    // Title
     case 1:                    // Value
        return String.class;
     case 2:                    // Display at top toggle
        return Boolean.class;
     default:
        // TODO  ITIMPI:  Not sure what we should return for this column - does it even matter?
        return Boolean.class;
   }
  }

  /**
   * Returns true if the cell at <code>rowIndex</code> and
   * <code>columnIndex</code>
   * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
   * change the value of that cell.
   *
   * @param   rowIndex        the row whose value to be queried
   * @param   columnIndex     the column whose value to be queried
   * @return  true if the cell is editable
   * @see #setValueAt
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
      case 1:
      case 2:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns the value for the cell at <code>columnIndex</code> and
   * <code>rowIndex</code>.
   *
   * @param   rowIndex        the row whose value is to be queried
   * @param   columnIndex     the column whose value is to be queried
   * @return  the value Object at the specified cell
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return getCustomCatalogs().get(rowIndex).getLabel();
      case 1:
        return getCustomCatalogs().get(rowIndex).getValue();
      case 2:
        return getCustomCatalogs().get(rowIndex).getAtTop();
      case 3:
        return Localization.Main.getText("config.CustomCatalogSavedSearchDelete.label");
      default:
        return "";
    }
  }

  /**
   * Sets the value in the cell at <code>columnIndex</code> and
   * <code>rowIndex</code> to <code>aValue</code>.
   *
   * @param   aValue           the new value
   * @param   rowIndex         the row whose value is to be changed
   * @param   columnIndex      the column whose value is to be changed
   * @see #getValueAt
   * @see #isCellEditable
   */
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    CustomCatalogEntry val = getCustomCatalogs().get(rowIndex);
    switch (columnIndex) {
      case 0:
        val.setLabel("" + aValue);
        break;
      case 1:
         val.setValue("" + aValue);
        break;
      case 2:
         val.setAtTop((Boolean)aValue);
    }
  }

  /**
   * Adds a listener to the list that is notified each time a change
   * to the data model occurs.
   *
   * @param   l               the TableModelListener
   */
  public void addTableModelListener(TableModelListener l) {
    if (!listeners.contains(l))
      listeners.add(l);
  }

  /**
   * Removes a listener from the list that is notified each time a
   * change to the data model occurs.
   *
   * @param   l               the TableModelListener
   */
  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }
}
