package com.gmail.dpierron.calibre.opds.i18n;

import com.gmail.dpierron.tools.Helper;

import java.lang.reflect.Method;

public class MethodCallerConverter implements Object2StringConverter {

  private String methodName;

  public MethodCallerConverter() {
    super();
  }

  public MethodCallerConverter(String methodName) {
    this();
    this.methodName = methodName;
  }

  String getMethodName() {
    return methodName;
  }

  void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  protected String callMethod(String methodName, Object o) {
    return callMethod(methodName, o, true);
  }

  protected String callMethod(String methodName, Object o, boolean returnObjectIfNull) {
    if (o == null)
      return null;

    if (methodName == null)
      return null;

    String result = null;

    // try calling a method on the object
    try {
      Method method = o.getClass().getDeclaredMethod(methodName);
      if (method != null)
        result = (String) method.invoke(o);
    } catch (Exception e) {
      // we don't give a tiny rat's ass
    }

    if (Helper.isNullOrEmpty(result) && returnObjectIfNull)
      result = o.toString();

    return result;
  }

  public String getStringValue(Object o) {
    return callMethod(getMethodName(), o);
  }

  public String getStringValueOrNull(Object o) {
    return callMethod(getMethodName(), o, false);
  }
}
