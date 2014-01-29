package com.gmail.dpierron.calibre.opds;
/**
 *
 */

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.thumbnails.CreateThumbnail;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public abstract class ImageManager {
  private final static Logger logger = Logger.getLogger(ImageManager.class);

  private boolean imageSizeChanged = false;
  private Map<File, File> imagesToGenerate;
  private long timeInImages = 0;

  private int imageHeight = 1;

  abstract String getResultFilename(Book book);
  abstract String getResultFilenameOld(Book book);

  abstract String getImageHeightDat();

  // CONSTRUCTOR

  ImageManager(int imageHeight) {
    reset();
    this.imageHeight = imageHeight;

    CachedFile imageSizeFile = CachedFileManager.INSTANCE.addCachedFile(ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder(), getImageHeightDat());
    FeedHelper.checkFileNameIsNewStandard(imageSizeFile, CachedFileManager.INSTANCE.addCachedFile(ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder(), imageSizeFile.getName().substring(4)));

    if (!imageSizeFile.exists())
      imageSizeChanged = true;
    else {
      try {
        ObjectInputStream ois = null;
        try {
          ois = new ObjectInputStream(new FileInputStream(imageSizeFile));
          int oldSize = ois.readInt();
          imageSizeChanged = oldSize != imageHeight;
        } finally {
          if (ois != null)
            ois.close();
          // TODO Need to update cachedFile information.
        }
      } catch (IOException e) {
        // we don't care about the file error, let's just say size has changed
        imageSizeChanged = true;
      }
    }
  }

  // METHODS and PROPERTIES

  public void reset () {
    imagesToGenerate = new HashMap<File, File>();
    timeInImages = 0;
  }
  public final static ThumbnailManager newThumbnailManager() {
    return new ThumbnailManager(ConfigurationManager.INSTANCE.getCurrentProfile().getThumbnailHeight());
  }

  public final static ImageManager newCoverManager() {
    return new CoverManager(ConfigurationManager.INSTANCE.getCurrentProfile().getCoverHeight());
  }

  public void setImageToGenerate(File reducedCoverFile, File coverFile) {
    if (!imagesToGenerate.containsKey(reducedCoverFile)) {
      imagesToGenerate.put(reducedCoverFile, coverFile);
    }
  }


  boolean hasImageSizeChanged() {
    return imageSizeChanged;
  }

  /**
   * Get the URI for a cover image.
   *
   * @param book
   * @return
   */
  String getImageUri(Book book) {
    String uriBase = ConfigurationManager.INSTANCE.getCurrentProfile().getUrlBooks();
    if (Helper.isNullOrEmpty(uriBase)) {
      uriBase = Constants.PARENT_PATH_PREFIX + Constants.PARENT_PATH_PREFIX;
    }
    return uriBase + FeedHelper.urlEncode(book.getPath() + "/" + getResultFilename(book), true);
  }

  public void writeImageHeightFile() {
    File imageSizeFile = new File(ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder(), getImageHeightDat());
    try {
      ObjectOutputStream oos = null;
      try {
        oos = new ObjectOutputStream(new FileOutputStream(imageSizeFile));
        oos.writeInt(imageHeight);
      } finally {
        if (oos != null)
          oos.close();
      }
    } catch (IOException e) {
      // we don't care if the image height file cannot be written, image will be recomputed and that's all
    }
  }

  public long generateImages() {
    long countFiles = 0;
    for (Map.Entry<File, File> fileEntry : imagesToGenerate.entrySet()) {
      File imageFile = fileEntry.getKey();
      if (logger.isDebugEnabled())
        logger.debug("generateImages: " + imageFile.getAbsolutePath());
      File coverFile = fileEntry.getValue();
      CatalogContext.INSTANCE.callback.incStepProgressIndicatorPosition();
      CatalogContext.INSTANCE.callback.showMessage(imageFile.getParentFile().getName() + File.separator + imageFile.getName());
      long now = System.currentTimeMillis();
      try {
        CreateThumbnail ct = new CreateThumbnail(coverFile.getAbsolutePath());
        ct.getThumbnail(imageHeight, CreateThumbnail.VERTICAL);
        ct.saveThumbnail(imageFile, CreateThumbnail.IMAGE_JPEG);
        // bug #732821 Ensure file added to those cached for copying
        CachedFile cf = CachedFileManager.INSTANCE.addCachedFile(imageFile);
        if (logger.isTraceEnabled())
          logger.trace("generateImages: added new thumbnail file " + imageFile.getAbsolutePath() + " to list of files to copy");
        countFiles++;         // Update count of files processed
      } catch (Exception e) {
        CatalogContext.INSTANCE.callback
            .errorOccured(Localization.Main.getText("error.generatingThumbnail", coverFile.getAbsolutePath()), e);
      } catch (Throwable t) {
           logger.warn("Unexpected error trying to generate image " + coverFile.getAbsolutePath() + "\n" + t );
      }
      timeInImages += (System.currentTimeMillis() - now);
    }
    writeImageHeightFile();
    return countFiles;
  }

  public int getNbImagesToGenerate() {
    return imagesToGenerate.size();
  }

}
