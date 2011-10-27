package com.gmail.dpierron.calibre.thumbnails;

import com.gmail.dpierron.calibre.opds.CatalogContext;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CreateThumbnail {
  private final static Logger logger = Logger.getLogger(CreateThumbnail.class);

  public static final int VERTICAL = 0;
  public static final int HORIZONTAL = 1;

  public static final String IMAGE_JPEG = "jpeg";
  public static final String IMAGE_JPG = "jpg";
  public static final String IMAGE_PNG = "png";

  private ImageIcon image;
  private ImageIcon thumb;

  public CreateThumbnail(String fileName) {
    image = new ImageIcon(fileName);
    if (!isImageLoaded()) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // I don't give a tiny rat's ass
      }
      if (!isImageLoaded())
        CatalogContext.INSTANCE.getCallback().errorOccured(Localization.Main.getText("error.loadingThumbnail", fileName), null);
    }
  }

  public boolean isImageLoaded() {
    return (image != null && image.getImageLoadStatus() == MediaTracker.COMPLETE);
  }

  public Image getThumbnail(int size, int dir) {
    if (dir == HORIZONTAL) {
      thumb = new ImageIcon(image.getImage().getScaledInstance(size, -1, Image.SCALE_SMOOTH));
    } else {
      thumb = new ImageIcon(image.getImage().getScaledInstance(-1, size, Image.SCALE_SMOOTH));
    }
    return thumb.getImage();
  }

  public Image getThumbnail(int size, int dir, int scale) {
    if (dir == HORIZONTAL) {
      thumb = new ImageIcon(image.getImage().getScaledInstance(size, -1, scale));
    } else {
      thumb = new ImageIcon(image.getImage().getScaledInstance(-1, size, scale));
    }
    return thumb.getImage();
  }

  public void saveThumbnail(File file, String imageType) {
    if (thumb != null) {
      BufferedImage bi = new BufferedImage(thumb.getIconWidth(), thumb.getIconHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics g = bi.getGraphics();
      g.drawImage(thumb.getImage(), 0, 0, null);
      int nbTries = 20;
      Exception exception = null;
      while (nbTries > 0) {
        boolean error = false;
        try {
          ImageIO.write(bi, imageType, file);
        } catch (IOException e) {
          logger.warn("error occured while writing image " + file.getName() + " - trying again (" + (nbTries) + " remaining)", e);
          if (exception == null)
            exception = e;
          error = true;
        }
        if (!error) {
          nbTries = 0;
          exception = null;
        } else
          nbTries--;
      }
      if (exception != null)
        CatalogContext.INSTANCE.getCallback().errorOccured(Localization.Main.getText("error.savingThumbnail", file.getAbsolutePath()), exception);
    } else {
      CatalogContext.INSTANCE.getCallback().errorOccured(Localization.Main.getText("error.generatingThumbnail", file.getAbsolutePath()), null);
    }
  }
}
