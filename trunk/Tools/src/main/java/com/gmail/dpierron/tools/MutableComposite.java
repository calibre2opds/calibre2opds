package com.gmail.dpierron.tools;

public class MutableComposite<T1, T2> {
  private T1 firstElement;
  private T2 secondElement;

  public MutableComposite(T1 firstElement, T2 secondElement) {
    this.firstElement = firstElement;
    this.secondElement = secondElement;
  }

  public T1 getFirstElement() {
    return firstElement;
  }

  public void setFirstElement(T1 firstElement) {
    this.firstElement = firstElement;
  }

  public T2 getSecondElement() {
    return secondElement;
  }

  public void setSecondElement(T2 secondElement) {
    this.secondElement = secondElement;
  }

}
