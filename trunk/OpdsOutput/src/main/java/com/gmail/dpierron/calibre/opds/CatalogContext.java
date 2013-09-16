package com.gmail.dpierron.calibre.opds;

/**
 * Container object for linking many object types used within the program
 *
 * NOTES:
 *   1.  We use public variables when getter/setters would have no additional logic!
 *       (this tends to lead to slightly more efficient Java code).
 */

public enum CatalogContext {
  INSTANCE;

  public CatalogManager catalogManager;
  public HtmlManager htmlManager;
  public ThumbnailManager thumbnailManager;
  public ImageManager coverManager;
  public CatalogCallbackInterface callback;
  public SecurityManager securityManager;

  private CatalogContext() {
    initialize();
  }

  public void initialize() {
    this.catalogManager = new CatalogManager();
    this.htmlManager = new HtmlManager();
    this.thumbnailManager = ImageManager.newThumbnailManager();
    this.coverManager = ImageManager.newCoverManager();
    this.securityManager = new SecurityManager();
  }
 /*
  public CatalogCallbackInterface getCallback() {
    if (callback == null)
      callback = new Log4jCatalogCallback();
    return callback;
  }

  public void setCallback(CatalogCallbackInterface callback) {
    this.callback = callback;
  }

  public CatalogManager getCatalogManager() {
    return catalogManager;
  }

  public void setCatalogManager(CatalogManager catalogManager) {
    this.catalogManager = catalogManager;
  }

  public HtmlManager getHtmlManager() {
    return htmlManager;
  }

  public void setHtmlManager(HtmlManager htmlManager) {
    this.htmlManager = htmlManager;
  }

  public ThumbnailManager getThumbnailManager() {
    return thumbnailManager;
  }

  public void setThumbnailManager(ThumbnailManager thumbnailManager) {
    this.thumbnailManager = thumbnailManager;
  }

  public ImageManager getCoverManager() {
    return coverManager;
  }

  public void setCoverManager(ImageManager coverManager) {
    this.coverManager = coverManager;
  }
  */

}
