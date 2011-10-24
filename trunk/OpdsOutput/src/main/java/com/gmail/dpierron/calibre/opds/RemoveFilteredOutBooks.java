package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.filter.*;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchNotFoundException;
import com.gmail.dpierron.tools.Helper;

public enum RemoveFilteredOutBooks {
  INSTANCE;

  public void runOnDataModel() throws CalibreSavedSearchInterpretException, CalibreSavedSearchNotFoundException {
    BooleanAndFilter andFilter = new BooleanAndFilter();

    // remove all books that have no ebook format in the included list
    andFilter.setLeftFilter(new SelectedEbookFormatsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludedFormatsList(),
        ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeBooksWithNoFile()));

    // remove all books not selected by the CatalogFilter search
    BookFilter mainCatalogFilter = null;
    String mainCatalogFilterOption = ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogFilter();

    if (Helper.isNotNullOrEmpty(mainCatalogFilterOption)) {
      mainCatalogFilter = CalibreQueryInterpreter.interpret(mainCatalogFilterOption);
    }
    if (mainCatalogFilter != null)
      andFilter.setRightFilter(mainCatalogFilter);

    FilterDataModel.INSTANCE.runOnDataModel(andFilter);
  }
}
