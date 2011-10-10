package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.filter.*;

public enum RemoveFilteredOutBooks {
  INSTANCE;

  public void runOnDataModel() {
    BooleanAndFilter level2BooleanAndFilter = new BooleanAndFilter();

    // remove all books that have no ebook format in the included list
    level2BooleanAndFilter.setLeftFilter(new SelectedEbookFormatsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludedFormatsList(),
        ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeBooksWithNoFile()));

    // remove all books that have no tag in the included list
    level2BooleanAndFilter.setRightFilter(new RequiredTagsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToGenerate()));

    BooleanAndFilter level1BooleanAndFilter = new BooleanAndFilter();

    // remove all books that have a tag in the excluded list
    level1BooleanAndFilter.setLeftFilter(level2BooleanAndFilter);
    level1BooleanAndFilter.setRightFilter(new ForbiddenTagsFilter(ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToExclude()));

    FilterDataModel.INSTANCE.runOnDataModel(level1BooleanAndFilter);
  }
}
