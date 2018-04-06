package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.i18n.Localization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageFile {
  private final static Logger logger = LogManager.getLogger(ImageFile.class);

  public static final int VERTICAL = 0;
  public static final int HORIZONTAL = 1;

  public static final String IMAGE_JPEG = "jpeg";
  public static final String IMAGE_JPG = "jpg";
  public static final String IMAGE_PNG = "png";

  private ImageIcon image;
  private ImageIcon thumb;

  public ImageFile(String fileName) {
    image = new ImageIcon(fileName);
    if (!isImageLoaded()) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // I don't give a tiny rat's ass
      }
      if (!isImageLoaded())
        CatalogManager.callback.errorOccured(Localization.Main.getText("error.loadingImage", fileName), null);
    }
  }

  public boolean isImageLoaded() {
    return (image != null && image.getImageLoadStatus() == MediaTracker.COMPLETE);
  }

  public Image getImage(int size, int dir) {
    if (dir == HORIZONTAL) {
      thumb = new ImageIcon(image.getImage().getScaledInstance(size, -1, Image.SCALE_SMOOTH));
    } else {
      thumb = new ImageIcon(image.getImage().getScaledInstance(-1, size, Image.SCALE_SMOOTH));
    }
    return thumb.getImage();
  }

  public void saveImage(File file, String imageType) {
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
        } catch (Exception e) {
          String msg = "error occurred while writing image " + file.getName() + " - trying again (" + (nbTries) + " remaining)";
          if (nbTries < 10) {
            logger.warn(msg, e);
            Helper.statsWarnings++;
          } else {
            logger.debug(msg, e);
          }
          if (exception == null)
            exception = e;
          error = true;
        }
        if (!error) {
          nbTries = 0;
          exception = null;
        } else {
          nbTries--;
          try {
            Thread.sleep(10); // wait 10ms
          } catch (InterruptedException e) {
            // I don't give a tiny rat's ass
          }
        }
      }
      if (exception != null)
        CatalogManager.callback.errorOccured(Localization.Main.getText("error.savingImage", file.getAbsolutePath()), exception);
    } else {
      CatalogManager.callback.errorOccured(Localization.Main.getText("error.generatingImage", file.getAbsolutePath()), null);
    }
  }
}
