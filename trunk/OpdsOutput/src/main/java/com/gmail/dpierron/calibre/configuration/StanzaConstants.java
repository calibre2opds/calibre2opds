package com.gmail.dpierron.calibre.configuration;


public class StanzaConstants {
  // true constants
  // embedded binary icons
  public final static String  ICON_RECENT = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIESURBVDjLlVJtaxpBEH7uvNSL50skFBqCiDVYpCWiIAjtx4Ih4I/zs78jkD9QioVAUBGNWigqRfpBxSO+3LnbmY13mNQWOvAwuzszz7zsQEoJBomWzWY/V6vVb5lM5oruBr/tYBQKhU+1Wu0r+/CbF6cOA02Tv9jr5gbn+TyGd3cQlQpe40nYFry9xZvLS/y8v8fm+lrZ0lJqukbCTlYwCCsWw3a7RTgex3EggLiuK5jkYkYiynYcjcLcEXOsvjvDNAx0BgPl1O31IIjEPjmBHQ5ja5rodLvK1nl48Ang9dgHRIyyN87O0LNtXFD2FLWmU4B0HKxdF99JDwhvhUCB9CPZLwDd2K/gw+kp3lsW5GYDl5wEg8heEdG7oyNkSGuE4GKBRyL1q6jX69J13b/CcRy5XC4VWPiNYzjWwAFZr9dot9tIp9Po9/uq9/l8jnK57H25L/ohAg4ejUaI0ORzuRxSqRRCoRAosw+P6BmB95inXfAWhdFqtVQ1Dg+UqqNW/Jg/WnhZ4mw2g6DJc/BkMlFnhud3cAb7ZNwOrbaaQzKZ5OXBcDiEQb/GA9XljoqU2A+u0CqzqVgswqKv5awcPB6PfSJ/Bgv6V5uEjoIN+wjQHrDmCjhzIpHAarVSLfktdGlNyTHKZf1LvAqYrNlsolQqPRFMp9MvjUbjI/5D6Dd+sP4NLTpNB1cxufkAAAAASUVORK5CYII=";
  public final static String  ICON_AUTHORS = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJ3SURBVDjLpZNtSNNRFIcNKunF1rZWBMJqKaSiX9RP1dClsjldA42slW0q5oxZiuHrlqllLayoaJa2jbm1Lc3QUZpKFmmaTMsaRp+kMgjBheSmTL2//kqMBJlFHx44XM7vOfdyuH4A/P6HFQ9zo7cpa/mM6RvCrVDzaVDy6C5JJKv6rwSnIhlFd0R0Up/GwF2KWyl01CTSkM/dQoQRzAurCjRCGnRUUE2FaoSL0HExiYVzsQwcj6RNrSqo4W5Gh6Yc4+1qDDTkIy+GhYK4nTgdz0H2PrrHUJzs71NQn86enPn+CVN9GnzruoYR63mMPbkC59gQzDl7pt7rc9f7FNyUhPY6Bx9gwt4E9zszhWWpdg6ZcS8j3O7zCTuEpnXB+3MNZkUUZu0NmHE8XsL91oSWwiiEc3MeseLrN6woYCWa/Zl8ozyQ3w3Hl2lYy0SwlCUvsVi/Gv2JwITnYPDun2Hy6jYuEzAF1jUBCVYpO6kXo+NuGMeBAgcgfwNkvgBOPgUqXgKvP7rBFvRhE1crp8Vq1noFYSlacVyqGk0D86gbART9BDk9BFnPCNJbCY5aCFL1Cyhtp0RWAp74MsKSrkq9guHyvfMTtmLc1togpZoyqYmyNoITzVTYRJCiXYBIQ3CwFqi83o3JDhX6C0M8XsGIMoQ4OyuRlq1DdZcLkmbgGDX1iIEKNxAcbgTEOqC4ZRaJ6Ub86K7CYFEo8Qo+GBQlQyXBczLZpbloaQ9k1NUz/kD2myBBKxRZpa5hVcQslalatoUxizxAVVrN3CW21bFj9F858Q9dnIRmDyeuybM71uxmH9BNBB1q6zybV7H9s1Ue4PM3/gu/AEbfqfWy2twsAAAAAElFTkSuQmCC";
  public final static String  ICON_TAGS = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAHcSURBVDjLhZPZihpBFIbrJeY2wbcQmjxdIGSSTC4zQxLyAK4o7igoKm7TPW49LoiYjqLG3DWpZmx7/tQpsR1xycW5qTr/9/+n+jTTdR3dbhftdhutVgvNZhOapkFVVTQajSsA7FKxTqcDx3GOajqdSki1Wr0IYeRMAsMwpPNkMnEhdCZSoFQqnYUwikzN5EYH9XpdNU0Ttm3LcwJWKhXk8/mTEEauu0YhfhKRDcuysDBt5H5tk4zHYxSLReRyuSMII+dd5M1mAxL//uvgw8Mz3t4DWWN7NxqNKAXS6fQBhIkZ+Wq1kk3r9Rpz4XytPeNLF/iqAx8f9pDhcEgpEI/HXQir1WpvxIx8uVzKps7Kls53AvCjB3x7PIQMBgNKgUgkIiGSUi6XFTEjXywWsunxj433qoM7fQ+51oDMzy2k1+tRCoRCoSt3lkKhoIgZ+Xw+P4J8F4DPTeDm3oK92aZIJpMIBAKvD15UzKdks1k+m81cyDsB+SRGuG2tYVpPL8Ued4SXlclklFQqxWkTCaILyG3bgWXvnf1+v8d9xFPLkUgklFgsxmkTd5+YxOL8QHwWQBWNRr3ipTktWL/fPym+CKAKh8PeYDDISezz+TwnV/l/v6tw9Qrxq3P3/wBazDrstPR7KQAAAABJRU5ErkJggg==";
  public final static String  ICON_SERIES = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJ+SURBVBgZBcExbFRlAADg7//fu7teC3elQEoMgeDkYDQ6oMQQTYyGxMHZuDA6Ypw0cWI20cHJUdl0cJLIiomR6OACGhUCpqGWtlzbu/b97/3v9/tCKQVc/e7RRXz+7OrSpUXbW7S9tu8ddv0M+3iCjF1s42v8WAP0XffKi2eOXfro9dMAYJ766SL1092jfDa17DfZgycHfvh7/hau1QB9161PhgE8epoNQlAHqprRIDo3iqoYDSpeOjv2zHRl7atfNj6LALltJys1Xc9+CmYtTxtmR8yO2D7kv4MMPr7x0KULK54/NThdA+S2XTs+jOYN86MsxqBGVRErKkEV6BHynp//2fXbw9lGDZBTWp+OK7PDzqIpYiyqSMxBFakUVYVS2dxrfHHrrz1crQG6lM6vTwZmR0UHhSoHsSBTKeoS9YU8yLrUXfj+w9d2IkBOzfkz05F5KkKkCkFERACEQil0TSOnJkMNV67fHNdVHI4GUcpZVFAUZAEExEibs4P5osMeROiadHoUiIEeCgFREAoRBOMB2weNrkmbNz+9UiBCTs1yrVdHqhgIkRL0EOj7QGG5jrZ2D+XUbADEy9dunOpSun7xuXMe7xUPNrOd/WyeyKUIoRgOGS8xWWZ7b6FLaROgzim9iXd+vXvf7mHtoCnaXDRtkLpel3t9KdamUx+8fcbj7YWc0hZAndv25XffeGH8yfuvAoBcaHOROhS+vLlhecD+wUJu222AOrft/cdPZr65ddfqsbHVyZLVlZHpysjx5aHRMBrV0XuX141qtnb25bb9F6Duu+7b23funb195955nMRJnMAJTJeGg8HS0sBkZWx1suz3Px79iZ8A/gd7ijssEaZF9QAAAABJRU5ErkJggg==";
  public final static String  ICON_BOOKS = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIASURBVDjLpVPPaxNREJ6Vt01caH4oWk1T0ZKlGIo9RG+BUsEK4kEP/Q8qPXnpqRdPBf8A8Wahhx7FQ0GF9FJ6UksqwfTSBDGyB5HkkphC9tfb7jfbtyQQTx142byZ75v5ZnZWC4KALmICPy+2DkvKIX2f/POz83LxCL7nrz+WPNcll49DrhM9v7xdO9JW330DuXrrqkFSgig5iR2Cfv3t3gNxOnv5BwU+eZ5HuON5/PMPJZKJ+yKQfpW0S7TxdC6WJaWkyvff1LDaFRAeLZj05MHsiPTS6hua0PUqtwC5sHq9zv9RYWl+nu5cETcnJ1M0M5WlWq3GsX6/T+VymRzHDluZiGYAAsw0TQahV8uyyGq1qFgskm0bHIO/1+sx1rFtchJhArwEyIQ1Gg2WD2A6nWawHQJVDIWgIJfLhQowTIeE9D0mKAU8qPC0220afsWFQoH93W6X7yCDJ+DEBeBmsxnPIJVKxWQVUwry+XyUwBlKMKwA8jqdDhOVCqVAzQDVvXAXhOdGBFgymYwrGoZBmUyGjxCCdF0fSahaFdgoTHRxfTveMCXvWfkuE3Y+f40qhgT/nMitupzApdvT18bu+YeDQwY9Xl4aG9/d/URiMBhQq/dvZMeVghtT17lSZW9/rAKsvPa/r9Fc2dw+Pe0/xI6kM9mT5vtXy+Nw2kU/5zOGRpvuMIu0YAAAAABJRU5ErkJggg==";
  public final static String  ICON_ABOUT = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAACXBIWXMAAA7EAAAOxAGVKw4bAAACm0lEQVR4nIXQzW5cRRAF4FM/1+OYQGRkIiVSBEKCp2bLNnt4gzwCwow8TiY4ZDz2/N6+3V11WCALBAZqV6X6Skcll2++g5ioA8hMZCp79HbY3fexMDtUTM1MBxc1U5+Jm+ugOjhJIIUBgSqIZKeCyOi1kCEQqFAV7jqYZ1iehJNOV4IiAIQiCBCdEEJFeqsRQUJV3DSiDeEYPJPOkAwHRZlIFQpBgC7SQTWN3murSIog1OhGr9HdMxgDh3AlCCDxRwJJAgChCiWzNERCmAaYh6c3zT7LYeg2eEYoVCRFJJMkIoJsoPTsUzmQAGiKSrjSzNxGMxWKc2r0gQYIyCSS0RmhQQPGXUmhCIShSQPVFJFouVpuvW7uTp48hamoEIgMsEc2RrgNdSyZCWS0BEJAtqy7cvXzZr7c+g/fvz775NOLF188//Ll+cV5KBQMBBCDShlL60FmtGBEtDjeHq9+2S/24y7Sf3zz3lzOTi4/P/OvXp1/8+2rF1+/tNNBjBG91nbYFQh6773k4fa4fHdcjOUIHAC7mM1qz2PNu0OfL7fz+c36+tcng5ydWtTpw7v1arVtpZdD3d6Vt8vjfJzuyZsaq9rs+elJJ8eIffQScWi5XI+Lq4+OfPbU16vt4u19rbHft9WqXh7Lx+RvtU+ZENpsNowZEwiFqwZYIvY159dr7/HZTH5arKca+31cHab3PTY1KIQQgMlgEzmSh+Smc5fZhRRU4vpm92zmN7fHXYnNxEWr25YpAAgAgNngQQSZZCI7OSYPmSmAyO3dKORuyg8R656BPyUA50PzMCOATqxbL2RNPQUSWPfs5F8lAMe/19gSg2hSgCn/Lv8HU1h6mGhkUB5Z+C8MIIhEAgQe0Y5Hbz7Uw2v1H5EB4HcxBixwKNtXgQAAAABJRU5ErkJggg==";
  public final static String  ICON_RATING = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAGvSURBVDjLpZO7alZREEbXiSdqJJDKYJNCkPBXYq12prHwBezSCpaidnY+graCYO0DpLRTQcR3EFLl8p+9525xgkRIJJApB2bN+gZmqCouU+NZzVef9isyUYeIRD0RTz482xouBBBNHi5u4JlkgUfx+evhxQ2aJRrJ/oFjUWysXeG45cUBy+aoJ90Sj0LGFY6anw2o1y/mK2ZS5pQ50+2XiBbdCvPk+mpw2OM/Bo92IJMhgiGCox+JeNEksIC11eLwvAhlzuAO37+BG9y9x3FTuiWTzhH61QFvdg5AdAZIB3Mw50AKsaRJYlGsX0tymTzf2y1TR9WwbogYY3ZhxR26gBmocrxMuhZNE435FtmSx1tP8QgiHEvj45d3jNlONouAKrjjzWaDv4CkmmNu/Pz9CzVh++Yd2rIz5tTnwdZmAzNymXT9F5AtMFeaTogJYkJfdsaaGpyO4E62pJ0yUCtKQFxo0hAT1JU2CWNOJ5vvP4AIcKeao17c2ljFE8SKEkVdWWxu42GYK9KE4c3O20pzSpyyoCx4v/6ECkCTCqccKorNxR5uSXgQnmQkw2Xf+Q+0iqQ9Ap64TwAAAABJRU5ErkJggg==";
  // external icon files
  public final static String  ICONFILE_RECENT = "recent.png";
  public final static String  ICONFILE_AUTHORS = "authors.png";
  public final static String  ICONFILE_TAGS = "tags.png";
  public final static String  ICONFILE_SERIES = "series.png";
  public final static String  ICONFILE_BOOKS = "allbooks.png";
  public final static String  ICONFILE_ABOUT = "c2o.png";
  public final static String  ICONFILE_RATING = "ratings.png";

  // options default values
  public final static String  LANGUAGE = "";
  public final static int     MAX_BEFORE_PAGINATE=25;
  public final static int     MAX_BEFORE_SPLIT=3*MAX_BEFORE_PAGINATE;
  public final static int     MAX_RECENT_ADDITIONS = 99999;
  public final static String  CATALOGFOLDER = "_catalog";
  public final static String  CATALOGTITLE = "Calibre library";
  public final static String  WIKIPEDIA_LANGUAGE = "en";
  public final static String  INCLUDEDFORMATS = "EPUB, PDF, RTF, TXT, PRC, PDB, MOBI, LRF, LRX, FB2";
  public final static boolean GENERATEOPDS = true;
  public final static boolean GENERATEHTML = true;
  public final static boolean GENERATEOPDSDOWNLOADS = true;
  public final static boolean GENERATEHTMLDOWNLOADS = true;
  public final static boolean GENERATEDOWNLOADS = true;
  public final static boolean SAVEBANDWIDTH = true;
  public final static boolean MINIMIZECHANGEDFILES = true;
  public final static boolean EXTERNALICONS = false;
  public final static int     THUMBNAIL_HEIGHT = 144;
  public final static boolean THUMBNAIL_GENERATE = true;
  public final static String  SPLITTAGSON = "";
  public final static String  FAVORITES_LIST = "";
  public final static boolean INCLUDEBOOKSWITHNOFILE = false;
  public final static String  DEFAULTBOOKLANGUAGETAG = "Lang:";
  public final static boolean CRYPT_FILENAMES = false;
  public final static boolean SHOWSERIESINAUTHORCATALOG = true;
  public final static int     MAX_SUMMARY_LENGTH = 30;
  public final static String  tagsToGenerate = "";
  public final static String  tagsToExclude = "";
  public final static boolean generateExternalLinks = true;
  public final static boolean generateCrossLinks = true;
  public final static boolean generateTags = true;
  public final static boolean generateRatings = true;
  public final static boolean generateAllbooks = true;
  public final static boolean suppressRatingsInTitles = false;
  public final static String  targetFolder = ".";
  public final static boolean COPYTODATABASEFOLDER = true;
  public final static boolean browseByCover = false;
  public final static boolean publishedDateAsYear = false;
  public final static boolean splitByAuthorInitialGoToBooks = false;
  public final static boolean includeAboutLink = true;
  public final static String  tagsToMakeDeep = "";
  public final static boolean browseByCoverWithoutSplit = true;
  public static final int     minBooksToMakeDeepLevel = 50;
  public final static boolean COVER_RESIZE = true;
  public static final int     CoverHeight = 550;
  public final static boolean IncludeOnlyOneFile = false;
  public final static CompatibilityTrick COMPATIBILITYTRICK = CompatibilityTrick.OPDS;
  public final static boolean ZipTrookCatalog = false;
  public final static boolean ReprocessEpubMetadata = false;
  public final static boolean OrderAllBooksBySeries = true;
  public final static int     MAX_MOBILE_RESOLUTION = 960;
  public final static boolean splitInSeriesBooks = false;
  public final static boolean splitInAuthorBooks = false;
  public final static String GOODREAD_ISBN_URL = "http://www.goodreads.com/book/isbn/{0}";
  public final static String GOODREAD_REVIEW_ISBN_URL = "http://www.goodreads.com/review/isbn/{0}";
  public final static String GOODREAD_TITLE_URL = "http://www.goodreads.com/book/title/{0}";
  public final static String GOODREAD_AUTHOR_URL = "http://www.goodreads.com/book/author/{0}";
  public final static String LIBRARYTHING_ISBN_URL = "http://www.librarything.com/isbn/{0}";
  public final static String LIBRARYTHING_TITLE_URL = "http://www.librarything.com/title/{0} {1}";
  public final static String LIBRARYTHING_AUTHOR_URL = "http://www.librarything.com/author/{0}";
  public final static String AMAZON_ISBN_URL = "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-isbn={0}";
  public final static String AMAZON_TITLE_URL = "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-title={0}&field-author={1}";
  public final static String AMAZON_AUTHOR_URL = "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-author={0}";
  public final static String ISFDB_AUTHOR_URL = "http://www.isfdb.org/cgi-bin/ea.cgi?{0}";
  public final static String WIKIPEDIA_URL = "http://{0}.wikipedia.org/wiki/{1}";

}
