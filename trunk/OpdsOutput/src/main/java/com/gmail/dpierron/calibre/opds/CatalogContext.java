package com.gmail.dpierron.calibre.opds;


public enum CatalogContext {
  INSTANCE;

  private CatalogManager catalogManager;
  private HtmlManager htmlManager;
  ImageManager thumbnailManager;
  ImageManager coverManager;
  private CatalogCallbackInterface callback;

  private CatalogContext() {
    initialize();
  }

  public void initialize() {
    this.catalogManager = new CatalogManager();
    this.htmlManager = new HtmlManager();
    this.thumbnailManager = ImageManager.newThumbnailManager();
    this.coverManager = ImageManager.newCoverManager();
  }

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

  public ImageManager getThumbnailManager() {
    return thumbnailManager;
  }

  public void setThumbnailManager(ImageManager thumbnailManager) {
    this.thumbnailManager = thumbnailManager;
  }

  public ImageManager getCoverManager() {
    return coverManager;
  }

  public void setCoverManager(ImageManager coverManager) {
    this.coverManager = coverManager;
  }

}
