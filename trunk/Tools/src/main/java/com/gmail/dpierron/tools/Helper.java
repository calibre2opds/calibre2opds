package com.gmail.dpierron.tools;

import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.dgc.VMID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Helper {

  private final static Logger logger = Logger.getLogger(Helper.class);
  private static final String DEFAULT_PREFIX = "com.gmail.dpierron.";
  static final int DEFAULT_STACK_DEPTH = 5;

  /**
   * checked s1.equals(s2)
   */
  public static final boolean stringEquals(String s1, String s2) {
    return objectEquals(s1, s2);
  }

  /**
   * checked s1.equals(s2)
   */
  public static final boolean objectEquals(Object o1, Object o2) {
    if (o1 == null || o2 == null)
      return (o1 == null && o2 == null);

    return o1.equals(o2);
  }

  public static String newId() {
    String result = new VMID().toString();
    result = result.replace('0', 'G');
    result = result.replace('1', 'H');
    result = result.replace('2', 'I');
    result = result.replace('3', 'J');
    result = result.replace('4', 'K');
    result = result.replace('5', 'L');
    result = result.replace('6', 'M');
    result = result.replace('7', 'N');
    result = result.replace('8', 'O');
    result = result.replace('9', 'P');
    result = result.replaceAll("-", "");
    result = result.replaceAll(":", "");
    return result;
  }

  public static void logStack(Logger logger) {
    logStack(logger, 2, DEFAULT_STACK_DEPTH, DEFAULT_PREFIX);
  }

  public static void logStack(Logger logger, int maxDepth) {
    logStack(logger, 2, maxDepth, DEFAULT_PREFIX);
  }

  public static void logStack(Logger logger, int skip, int maxDepth) {
    logStack(logger, skip + 2, maxDepth, DEFAULT_PREFIX);
  }

  public static void logStack(Logger logger, int skip, int maxDepth, String removePrefix) {
    Exception e = new RuntimeException();
    e.fillInStackTrace();
    StackTraceElement[] calls = e.getStackTrace();
    for (int i = skip; i < maxDepth + skip; i++) {
      if (i >= calls.length)
        break;
      StackTraceElement call = e.getStackTrace()[i];
      String msg = call.toString();
      if (removePrefix != null) {
        int pos = msg.indexOf(removePrefix);
        if (pos > -1)
          msg = msg.substring(pos + removePrefix.length());
      }
      if (logger.isDebugEnabled())
        logger.debug("Stack trace [" + (i - skip) + "] -> " + msg);
    }
  }

  public static String concatenateList(Collection toConcatenate) {
    return concatenateList(null, toConcatenate, (String[]) null);
  }

  public static String concatenateList(String separator, Collection toConcatenate) {
    return concatenateList(separator, toConcatenate, (String[]) null);
  }

  public static String concatenateList(Collection toConcatenate, String... methodName) {
    return concatenateList(null, toConcatenate, methodName);
  }

  public static String concatenateList(String separator, Collection toConcatenate, String... methodName) {
    try {
      if (toConcatenate == null || toConcatenate.size() == 0)
        return "";
      StringBuffer lines = new StringBuffer();
      String theSeparator = (separator == null ? ", " : separator);
      for (Object o : toConcatenate) {
        Object result = o;
        if (methodName == null) {
          result = o.toString();
        } else {
          for (String theMethodName : methodName) {
            Method method = null;
            try {
              method = result.getClass().getMethod(theMethodName);
            } catch (SecurityException e) {
              logger.warn(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
              logger.warn(e.getMessage(), e);
            }
            if (method != null)
              try {
                result = method.invoke(result);
              } catch (IllegalArgumentException e) {
                logger.warn(e.getMessage(), e);
              } catch (IllegalAccessException e) {
                logger.warn(e.getMessage(), e);
              } catch (InvocationTargetException e) {
                logger.warn(e.getMessage(), e);
              }
          }
        }
        lines.append(result);
        lines.append(theSeparator);
      }
      String sLines = lines.length() >= theSeparator.length() ? lines.substring(0, lines.length() - theSeparator.length()) : "";
      return sLines;
    } catch (Exception e) {
      return "";
    }
  }

  public static Collection transformList(Collection toTransform, String... methodName) {
    List transformed = new LinkedList();
    try {
      if (toTransform == null || toTransform.size() == 0)
        return toTransform;
      for (Object o : toTransform) {
        Object result = o;
        if (methodName == null) {
          result = o.toString();
        } else {
          for (String theMethodName : methodName) {
            Method method;
            method = result.getClass().getMethod(theMethodName);
            result = method.invoke(result);
          }
        }
        transformed.add(result);
      }
      return transformed;
    } catch (Exception e) {
      return transformed;
    }
  }

  public static List<String> tokenize(String text, String delim) {
    return tokenize(text, delim, false);
  }

  public static List<String> tokenize(String text, String delim, boolean trim) {
    List<String> result = new LinkedList<String>();
    if (isNotNullOrEmpty(text)) {
      String s = text.replaceAll(delim, "�");
      String[] tokens = s.split("�");
      for (String token : tokens) {
        if (trim)
          token = token.trim();
        result.add(token);
      }
    }
    return result;
  }

  public static String deTokenize(Collection<String> list, String delim) {
    if (list.size() > 0) {
      StringBuffer sb = new StringBuffer();
      for (String text : list) {
        if (sb.length() > 0)
          sb.append(delim);
        sb.append(text);
      }
      return sb.toString();
    } else
      return "";
  }

  public static String leftPad(String s, char paddingCharacter, int length) {
    if (s.length() >= length)
      return s;
    StringBuffer sb = new StringBuffer();
    for (int i = s.length(); i < length; i++) {
      sb.append(paddingCharacter);
    }
    sb.append(s);
    return sb.toString();
  }

  public static String pad(String s, char paddingCharacter, int length) {
    if (s.length() >= length)
      return s;
    StringBuffer sb = new StringBuffer(s);
    for (int i = s.length(); i < length; i++) {
      sb.append(paddingCharacter);
    }
    return sb.toString();
  }

  /**
   * @return the last numeric component of string s (that is, the part of the
   *         string to the right of the last non-numeric character)
   */
  public static String getLastNumericComponent(String s) {
    int i = s.length() - 1;
    while ((i >= 0) && (Character.isDigit(s.charAt(i))))
      i--;
    if (i < 0)
      return s;
    if (i == s.length() - 1 && !Character.isDigit(s.charAt(i)))
      return null;
    return (s.substring(i + 1));
  }

  /**
   * @return the right part of string s, after the first occurence of string
   *         toSubstract, or the whole string s if it does not contain
   *         toSubstract
   */
  public static String substractString(String s, String toSubstract) {
    if (isNullOrEmpty(s))
      return s;
    if (isNullOrEmpty(toSubstract))
      return s;
    if (!s.startsWith(toSubstract))
      return s;
    return s.substring(toSubstract.length());
  }

  public static boolean trueBooleanEquals(Object o1, Object o2) {
    return trueBoolean(o1) == trueBoolean(o2);
  }

  public static boolean trueBoolean(Object o) {
    if (o == null)
      return false;
    if (o instanceof Boolean)
      return ((Boolean) o).booleanValue();
    else
      return new Boolean(o.toString()).booleanValue();
  }

  public static Integer parseInteger(String s) {
    if (isNullOrEmpty(s))
      return null;
    try {
      int i = Integer.parseInt(s);
      return new Integer(i);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static boolean trueStringEquals(Object o1, Object o2) {
    return trueString(o1).equals(trueString(o2));
  }

  public static int trueStringCompare(Object o1, Object o2) {
    return trueString(o1).compareTo(trueString(o2));
  }

  public static String trueString(Object o) {
    if (o == null)
      return "";
    else
      return o.toString();
  }

  public static Object[] arrayThis(Object[] theArray, Object... objects) {
    ArrayList result = listThis(objects);
    return result.toArray(theArray);
  }

  public static ArrayList listThis(Object... objects) {
    return listThis(false, objects);
  }

  public static ArrayList listThis(boolean duplicates, Object... objects) {
    ArrayList result = new ArrayList();
    if (objects != null)
      for (Object object : objects) {
        if (object == null)
          continue;
        if (object instanceof List) {
          List list = (List) object;
          if (duplicates) {
            result.addAll(list);
          } else {
            if (list != null)
              for (Object item : list) {
                if (item == null)
                  continue;
                if (!result.contains(item))
                  result.add(item);
              }
          }
        } else {
          result.add(object);
        }
      }
    return result;
  }

  /**
   * This method takes a string and wraps it to a line length of no more than
   * wrap_length. If prepend is not null, each resulting line will be prefixed
   * with the prepend string. In that case, resultant line length will be no
   * more than wrap_length + prepend.length()
   */

  public static String wrap(String inString, int wrap_length, String prepend) {
    char[] charAry;

    int p, p2, offset = 0, marker;

    StringBuffer result = new StringBuffer();

    /* -- */

    if (inString == null) {
      return null;
    }

    if (wrap_length < 0) {
      throw new IllegalArgumentException("bad params");
    }

    if (prepend != null) {
      result.append(prepend);
    }

    charAry = inString.toCharArray();

    p = marker = 0;

    // each time through the loop, p starts out pointing to the same char as
    // marker

    while (marker < charAry.length) {
      while (p < charAry.length && (charAry[p] != '\n') && ((p - marker) < wrap_length)) {
        p++;
      }

      if (p == charAry.length) {
        result.append(inString.substring(marker, p));
        return result.toString();
      }

      if (charAry[p] == '\n') {
        /*
         * We've got a newline. This newline is bound to have terminated the
         * while loop above. Step p back one character so that the isspace(*p)
         * check below will detect that it hit the \n, and will do the right
         * thing.
         */

        result.append(inString.substring(marker, p + 1));

        if (prepend != null) {
          result.append(prepend);
        }

        p = marker = p + 1;

        continue;
      }

      p2 = p - 1;

      /*
       * We've either hit the end of the string, or we've gotten past the
       * wrap_length. Back p2 up to the last space before the wrap_length, if
       * there is such a space. Note that if the next character in the string
       * (the character immediately after the break point) is a space, we don't
       * need to back up at all. We'll just print up to our current location, do
       * the newline, and skip to the next line.
       */

      if (p < charAry.length) {
        if (isspace(charAry[p])) {
          offset = 1; /*
                       * the next character is white space. We'll want to skip
                       * that.
                       */
        } else {
          /* back p2 up to the last white space before the break point */

          while ((p2 > marker) && !isspace(charAry[p2])) {
            p2--;
          }

          offset = 0;
        }
      }

      /*
       * If the line was completely filled (no place to break), we'll just copy
       * the whole line out and force a break.
       */

      if (p2 == marker) {
        p2 = p - 1;
      }

      if (!isspace(charAry[p2])) {
        /*
         * If weren't were able to back up to a space, copy out the whole line,
         * including the break character (in this case, we'll be making the
         * string one character longer by inserting a newline).
         */

        result.append(inString.substring(marker, p2 + 1));
      } else {
        /*
         * The break character is whitespace. We'll copy out the characters up
         * to but not including the break character, which we will effectively
         * replace with a newline.
         */

        result.append(inString.substring(marker, p2));
      }

      /* If we have not reached the end of the string, newline */

      if (p < charAry.length) {
        result.append("\n");

        if (prepend != null) {
          result.append(prepend);
        }
      }

      p = marker = p2 + 1 + offset;
    }

    return result.toString();
  }

  public static String wrap(String inString, int wrap_length) {
    return wrap(inString, wrap_length, null);
  }

  public static String wrap(String inString) {
    return wrap(inString, 150, null);
  }

  public static boolean isspace(char c) {
    return (c == '\n' || c == ' ' || c == '\t');
  }

  /**
   * checks whether the object o is contained in the collection c
   *
   * @param comparator if not null, used to determine if two items are the same
   */
  public static boolean contains(Collection c, Object o, Comparator comparator) {
    if (comparator == null) {
      // simply check if 'c' contains the pointer 'o'
      return c.contains(o);
    } else {
      // look into 'c' for occurence of 'o'
      for (Object o2 : c) {
        if (comparator.compare(o, o2) == 0) { // the objects match
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Computes the intersection between two collections
   *
   * @param c1 the first collection
   * @param c2 the second (obviously) collection
   */
  public static Collection intersect(Collection c1, Collection c2) {
    return intersect(c1, c2, null);
  }

  /**
   * Computes the intersection between two collections
   *
   * @param c1         the first collection
   * @param c2         the second (obviously) collection
   * @param comparator is used to determine if two items are the same
   */
  public static Collection intersect(Collection c1, Collection c2, Comparator comparator) {

    Collection result = new LinkedList();

    if (c1 == null || c2 == null || c1.size() == 0 || c2.size() == 0)
      return result;

    /*
     * This algorithm is shamelessly stolen from Collections.disjoint() ...
     * 
     * We're going to iterate through c1 and test for inclusion in c2. If c1 is
     * a Set and c2 isn't, swap the collections. Otherwise, place the shorter
     * collection in c1. Hopefully this heuristic will minimize the cost of the
     * operation.
     */
    Collection left = c1;
    Collection right = c2;
    if ((left instanceof Set) && !(right instanceof Set) || (left.size() > right.size())) {

      left = c2;
      right = c1;
    }

    for (Object l : left) {
      if (contains(right, l, comparator))
        result.add(l);
    }
    return result;
  }

  /**
   * Returns true if the object specified is nor null nor empty (e.g., an empty
   * string, or an empty collection)
   *
   * @param object the object to check
   * @return true if the text specified is nor null nor empty, false otherwise
   */
  public final static boolean isNotNullOrEmpty(Object object) {
    return !(isNullOrEmpty(object));
  }

  /**
   * Returns true if the object specified is nor null nor empty (e.g., an empty
   * string, or an empty collection, or in this case a zero-valued number)
   *
   * @param object the object to check
   * @return true if the text specified is null or empty, false otherwise
   */
  public final static boolean isNotNullOrEmptyOrZero(Object object) {
    return !(isNullOrEmpty(object, true));
  }

  /**
   * Returns true if the object specified is null or empty (e.g., an empty
   * string, or an empty collection)
   *
   * @param object the object to check
   * @return true if the text specified is null or empty, false otherwise
   */
  public final static boolean isNullOrEmpty(Object object) {
    return (isNullOrEmpty(object, false));
  }

  /**
   * Returns true if the object specified is null or empty (e.g., an empty
   * string, or an empty collection, or in this case a zero-valued number)
   *
   * @param object the object to check
   * @return true if the text specified is null or empty, false otherwise
   */
  public final static boolean isNullOrEmptyOrZero(Object object) {
    return (isNullOrEmpty(object, true));
  }

  private final static boolean isNullOrEmpty(Object object, boolean zeroEqualsEmpty) {
    if (object == null)
      return true;
    if (object instanceof Collection)
      return ((Collection) object).size() == 0;
    else if (object instanceof Map)
      return ((Map) object).size() == 0;
    else if (object.getClass().isArray())
      return ((Object[]) object).length == 0;
    else if (object instanceof Number && zeroEqualsEmpty)
      return ((Number) object).longValue() == 0;
    else
      return object.toString().length() == 0;
  }

  /**
   * Returns the list of files corresponding to the extension, in the currentDir
   * and below
   */
  public static List<File> recursivelyGetFiles(final String extension, File currentDir) {
    List<File> result = new LinkedList<File>();
    recursivelyGetFiles(extension, currentDir, result);
    return result;
  }

  private static void recursivelyGetFiles(final String extension, File currentDir, List<File> filesList) {
    String[] files = currentDir.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        return f.isDirectory() || name.endsWith(extension);
      }

    });

    for (String filename : files) {
      File f = new File(currentDir, filename);
      if (f.isDirectory())
        recursivelyGetFiles(extension, f, filesList);
      else
        filesList.add(f);
    }
  }

  /**
   * forces a string into a specific encoding
   */
  public static String forceCharset(String text, String charset) {
    String result = text;
    try {
      result = new String(text.getBytes(charset));
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
    }
    return result;
  }

  public static Object getDelegateOrNull(Object source) {
    return getDelegateOrNull(source, false);
  }

  public static Object getDelegateOrNull(Object source, boolean digDeep) {
    if (source == null)
      return null;
    Object result = source;
    try {
      Method method = source.getClass().getMethod("getDelegate", (Class[]) null);
      if (method != null)
        result = method.invoke(source, (Object[]) null);
    } catch (SecurityException e) {
      // let's return the source object
    } catch (NoSuchMethodException e) {
      // let's return the source object
    } catch (IllegalArgumentException e) {
      // let's return the source object
    } catch (IllegalAccessException e) {
      // let's return the source object
    } catch (InvocationTargetException e) {
      // let's return the source object
    }
    if (digDeep && (result != source))
      return getDelegateOrNull(result, digDeep);
    else
      return result;
  }

  public static List filter(List source, List unwantedKeys, String method) {
    if (isNullOrEmpty(unwantedKeys))
      return source;
    if (isNullOrEmpty(source))
      return source;
    List result = new LinkedList();
    for (Object object : source) {
      if (object == null)
        continue;
      Object key = null;
      try {
        Method keyGetter = object.getClass().getMethod(method, (Class[]) null);
        if (keyGetter != null)
          key = keyGetter.invoke(object, (Object[]) null);
      } catch (SecurityException e) {
        // key stays null
      } catch (NoSuchMethodException e) {
        // key stays null
      } catch (IllegalArgumentException e) {
        // key stays null
      } catch (IllegalAccessException e) {
        // key stays null
      } catch (InvocationTargetException e) {
        // key stays null
      }
      if (key == null)
        key = object.toString();
      if (key != null && !unwantedKeys.contains(key))
        result.add(object);
    }
    return result;
  }

  public static List filter(List source, List<Class> unwantedClasses) {
    if (isNullOrEmpty(unwantedClasses))
      return source;
    if (isNullOrEmpty(source))
      return source;
    List result = new LinkedList();
    for (Object object : source) {
      if (object == null)
        continue;
      if (!unwantedClasses.contains(object.getClass()))
        result.add(object);
    }
    return result;
  }

  /**
   * Fetch the entire contents of a text stream, and return it in a String.
   * This style of implementation does not throw Exceptions to the caller.
   */
  public static String readTextFile(InputStream is) {
    //...checks on aFile are elided
    StringBuilder contents = new StringBuilder();

    try {
      //use buffering, reading one line at a time
      //FileReader always assumes default encoding is OK!
      BufferedReader input = new BufferedReader(new InputStreamReader(is));
      try {
        String line = null; //not declared within while loop
        /*
        * readLine is a bit quirky :
        * it returns the content of a line MINUS the newline.
        * it returns null only for the END of the stream.
        * it returns an empty String if two newlines appear in a row.
        */
        while ((line = input.readLine()) != null) {
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }
      } finally {
        input.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return contents.toString();
  }

  public static String readTextFile(String fullPathFilename) throws IOException {
    return readTextFile(new FileInputStream(fullPathFilename));
  }

  public static File putBytesIntoFile(byte[] bytes, File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    try {
      out.write(bytes);
    } finally {
      out.close();
    }
    return file;
  }

  public static File getFileFromBytes(byte[] bytes) throws IOException {
    String prefix = new VMID().toString();
    return getFileFromBytes(bytes, prefix);
  }

  public static File getFileFromBytes(byte[] bytes, String prefix) throws IOException {
    File f = File.createTempFile(prefix, null);
    return putBytesIntoFile(bytes, f);
  }

  public static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    // You cannot create an array using a long type.
    // It needs to be an int type.
    // Before converting to an int type, check
    // to ensure that file is not larger than Integer.MAX_VALUE.
    if (length > Integer.MAX_VALUE) {
      // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int) length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file " + file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
  }

  public static String nowAs14CharString() {
    final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    return format.format(new Date());
  }

  public static String removeLeadingZeroes(String value) {
    return removeLeadingChars(value, '0');
  }

  public static String removeLeadingChars(String value, char charToRemove) {
    if (Helper.isNotNullOrEmpty(value)) {
      String regex = "^" + charToRemove + "*";
      return value.replaceAll(regex, "");
    }
    return value;
  }

  public static String makeString(String baseString, int number) {
    if (Helper.isNotNullOrEmpty(baseString) && (number > 0)) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < number; i++)
        sb.append(baseString);
      return sb.toString();
    }
    return "";
  }

  public static String getStackTrace(Throwable t) {
    if (t == null)
      return null;
    StringBuffer sb = new StringBuffer();
    for (StackTraceElement element : t.getStackTrace()) {
      sb.append(element.toString());
      sb.append('\n');
    }
    return sb.toString();
  }

  public static Stack<Throwable> unwrap(Throwable t) {
    Stack<Throwable> result = new Stack<Throwable>();
    while (t != null) {
      result.add(t);
      t = t.getCause();
    }
    return result;
  }

  public static class ListCopier<T> {
    public List<T> copyList(List<T> original, int pMaxSize) {
      if (original == null)
        return null;
      int maxSize = pMaxSize;
      if (maxSize < 0)
        maxSize = original.size();
      if (original.size() <= maxSize)
        return new LinkedList<T>(original);
      List<T> result = new LinkedList<T>();
      for (int i = 0; i < maxSize; i++) {
        result.add(original.get(i));
      }
      return result;
    }
  }


  public static String shorten(String s, int maxSize) {
    if (isNullOrEmpty(s))
      return s;
    if (s.length() > maxSize)
      return s.substring(0, maxSize) + "...";
    else
      return s;
  }


  // ITIMPI:  It appears this routine is no longer used!
  //          the logic has been subsumed into Catalog.merge()
  /*
    public static void copy(File src, File dst, boolean checkDates) throws IOException {
      if (src == null || dst == null) return;

      if (!src.exists()) return;

      if (src.isDirectory()) {

        if (!dst.exists()) dst.mkdirs();
        else if (!dst.isDirectory()) return;
      } else {

        boolean copy = false;
        if (!dst.exists()) {
          // ITIMPI:  Is there a reason this is not logged via logger()?
          System.out.println("creating dirs " + dst.getPath());
          dst.mkdirs();
          copy = true;
        }
        copy = copy || (!checkDates || (src.lastModified() > dst.lastModified()));
        if (copy) {
          InputStream in = new FileInputStream(src);
          copy(in, dst);
        }
      }
    }
  */

  public static void copy(File src, File dst) throws IOException {
    InputStream in = null;
    try {
      in = new FileInputStream(src);
      Helper.copy(in, dst);
    } finally {
      if (in != null)
        in.close();
    }

  }

  public static void copy(InputStream in, File dst) throws IOException {
    if (!dst.exists())
      dst.getParentFile().mkdirs();
    OutputStream out = null;
    try {
      out = new FileOutputStream(dst);

      copy(in, out);

    } finally {
      if (out != null)
        out.close();
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    try {
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      if (out != null)
        out.close();
    }
  }

  static public void delete(File path) {
    if (path.exists()) {
      if (path.isDirectory()) {
        File[] files = path.listFiles();
        if (files != null)
          for (int i = 0; i < files.length; i++) {
            delete(files[i]);
          }
      }
      boolean result = path.delete();
      if (!result) {
        logger.warn("couldn't delete " + path.getAbsolutePath());
      }
    }
  }

  static public long count(File path) {
    int result = 0;
    if (path.exists()) {
      if (path.isDirectory()) {
        File[] files = path.listFiles();
        if (files != null)
          for (int i = 0; i < files.length; i++) {
            result += count(files[i]);
          }
      }
      result++;
    }
    return result;
  }

  public static int checkedCompare(Comparable o1, Comparable o2) {
    if (o1 == null) {
      if (o2 == null)
        return 0;
      else
        return 1;
    } else if (o2 == null) {
      if (o1 == null)
        return 0;
      else
        return -1;
    }
    return o1.compareTo(o2);
  }

  public static ArrayList<File> listFilesIn(File dir) {
    ArrayList<File> result = new ArrayList<File>();
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      if (children != null) {
        for (String childName : children) {
          File child = new File(dir, childName);
          if (child.isDirectory()) {
            result.addAll(listFilesIn(child));
          }
          result.add(child);
        }
      }
    }
    return result;
  }

  public static void recursivelyZipFiles(File inFolder, File outZipFile) throws IOException {
    recursivelyZipFiles(null, false, inFolder, outZipFile);
  }

  public static void recursivelyZipFiles(File inFolder, boolean includeNameOfOriginalFolder, File outZipFile) throws IOException {
    recursivelyZipFiles(null, includeNameOfOriginalFolder, inFolder, outZipFile);
  }

  public static void recursivelyZipFiles(final String extension, boolean includeNameOfOriginalFolder, File inFolder, File outZipFile) throws IOException {
    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outZipFile)));
    String relativePath = "";
    if (includeNameOfOriginalFolder)
      relativePath = inFolder.getName();
    try {
      recursivelyZipFiles(extension, relativePath, inFolder, zipOutputStream);
    } finally {
      zipOutputStream.close();
    }
  }

  private static void recursivelyZipFiles(final String extension, String currentRelativePath, File currentDir, ZipOutputStream zipOutputStream) throws
      IOException {
    String[] files = currentDir.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        if (extension == null)
          return true;
        else
          return f.isDirectory() || name.endsWith(extension);
      }

    });

    for (String filename : files) {
      File f = new File(currentDir, filename);
      String fileRelativePath = currentRelativePath + (Helper.isNullOrEmpty(currentRelativePath) ? "" : File.separator) + filename;
      if (f.isDirectory())
        recursivelyZipFiles(extension, fileRelativePath, f, zipOutputStream);
      else {
        BufferedInputStream in = null;
        byte[] data = new byte[1024];
        in = new BufferedInputStream(new FileInputStream(f), 1000);
        zipOutputStream.putNextEntry(new ZipEntry(fileRelativePath));
        int count;
        while ((count = in.read(data, 0, data.length)) != -1) {
          zipOutputStream.write(data, 0, count);
        }
        zipOutputStream.closeEntry();
      }
    }
  }

  public static String toTitleCase(String s) {
    if (Helper.isNullOrEmpty(s))
      return s;

    StringBuffer sb = new StringBuffer(s.length());
    sb.append(Character.toUpperCase(s.charAt(0)));
    sb.append(s.substring(1));
    return sb.toString();
  }

  public static String removeHtmlElements(String s) {
    // process possible HTML tags
    StringBuffer sb = new StringBuffer();
    if (s != null) {
      boolean skipping = false;
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (!skipping) {
          if (c == '<')
            skipping = true;
          else
            sb.append(c);
        } else {
          if (c == '>')
            skipping = false;
        }
      }
    } else {
      return "";
    }
    return sb.toString();
  }
}
