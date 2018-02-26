package com.gmail.dpierron.calibre.datamodel;

/**
 Class that enumerates the different column types that are supported by Calibre.
 This icnludes both the built-in columns and the custom columns.

 It is used to determine how a sub-catalog should be built from data in this
 column type.  Is part of the logic to try and genericise the code to cover
 both the built-in columns and generic columns
 */
public enum ColumType {
  COLUMN_BOOK,              // Pseudo type to describe book sub-catalog
  COLUMN_AUTHOR,            // Text with values separate by &
  COLUMN_SERIES,            // Serie and associated Series number
  COLUMN_TAGS,              // Text with values seaprated by ,
  COLUMN_DATETIME,          // Date/Time field
  COLUMN_YESNO,             // Yes/No (special boolean type)
  COLUMN_BOOLEAN,           // Boolean
  COLUMN_ENUMERATION,       // Text with limited list of permitted values
  COLUMN_RATING,            // Rating (normally displayed as stars)
  COLUMN_TEXT,              // Short Text
  COLUMN_LONGTEXT,          // Long text - often contains embedded HTML
  COLUMN_FLOAT,             // Floating point values
  COLUMN_INTEGER,           // Integer values
  COLUMN_BUILT,             // Text (Built from other columns)
  COLUMN_BUILT_TAGS;        // Tag-like list 9built from other columns)
}
