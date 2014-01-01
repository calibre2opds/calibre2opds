package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

public class ProgressIndicator {
  Logger logger = Logger.getLogger(ProgressIndicator.class);

  long maxVisible;
  double scale;
  int pos;
  int position;
  char indicator = '*';

  public ProgressIndicator() {
    super();
    setIndicator('*');
    setMaxVisible(20);
  }

  public ProgressIndicator setIndicator(char c) {
    this.indicator = c;
    return this;
  }

  public ProgressIndicator setMaxVisible(long maxVisible) {
    this.maxVisible = maxVisible;
    return this;
  }

  public void reset() {
    pos = 0;
    position = 0;
    actOnMessage("");
  }

  public void setMaxScale(long maxScale) {
    logger.debug("maxScale=" + maxScale);
    reset();
    scale = (double) maxVisible / (double) maxScale;
  }

  public void actOnPositionChange(int newPos) {
    logger.info(Helper.pad("", indicator, newPos));
  }

  public void actOnMessage(String message) {
    logger.trace(message);
  }

  public void incPosition() {
    position++;
    int newPos = (int) (position * scale);
    if (newPos > pos) {
      actOnPositionChange(newPos);
      pos = newPos;
    }
  }

  public int getPosition() {
    return position;
  }
}
