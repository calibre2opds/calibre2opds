package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.filter.*;

public enum RemoveFilteredOutBooks {
  INSTANCE;

  public void runOnDataModel() {
    BooleanFilter booleanFilter = new BooleanFilter();

    // remove all books that have no ebook format in the included list
    booleanFilter.addFilter(new SelectedEbookFormatsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludedFormatsList(),
        ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeBooksWithNoFile()));

    // remove all books that have no tag in the included list
    booleanFilter.addFilter(new RequiredTagsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToGenerate(), false));

    // remove all books that have a tag in the excluded list
    booleanFilter.addFilter(new ForbiddenTagsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToExclude(), false));

    FilterDataModel.INSTANCE.runOnDataModel(booleanFilter);
  }
}
