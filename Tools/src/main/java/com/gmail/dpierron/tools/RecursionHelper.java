package com.sag.baf.tools;

import java.util.Collection;
import java.util.Stack;

/**
 * This helper class is easy to override, and helps create a recursive function local to a method (nested functions don't exist in Java)
 *
 * @param <T> the type of object that we recurse on
 */
public abstract class RecursionHelper<T> {
  private boolean depthFirst = false;
  private Stack<T> currentRecursionStack;
  private T startElement;

  /**
   * sets the recursionHelper to go down the depth of the tree first, before doing any action on the nodes
   *
   * @return
   */
  public RecursionHelper<T> setDepthFirst() {
    this.depthFirst = true;
    return this;
  }

  /**
   * gets the next level of recursion
   *
   * @param currentElement is never null
   * @return the list of elements that are contained in currentElement
   */
  public abstract Collection<T> getRecursedElements(T currentElement);

  /**
   * do the desired action on each recursed elements
   *
   * @param currentElement is never null
   */
  public abstract void doActionOnElement(T currentElement);

  public RecursionHelper(T startElement) {
    this.startElement = startElement;
  }

  public int getCurrentRecursionLevel() {
    if (currentRecursionStack == null)
      return 0;
    return currentRecursionStack.size();
  }

  public Stack<T> getCurrentRecursionStack() {
    return currentRecursionStack;
  }

  public RecursionHelper<T> start() {
    currentRecursionStack = new Stack<T>();
    recurseOn(startElement);
    return this;
  }

  void recurseOn(T currentElement) {
    if (currentElement == null)
      return;
    if (currentRecursionStack.contains(currentElement))
      throw new RuntimeException("Circular recursion !");
    currentRecursionStack.push(currentElement);
    if (!depthFirst)
      doActionOnElement(currentElement);
    Collection<T> nextLevelElements = getRecursedElements(currentElement);
    if (nextLevelElements != null) {
      for (T nextLevelElement : getRecursedElements(currentElement)) {
        recurseOn(nextLevelElement);
      }
    }
    if (depthFirst)
      doActionOnElement(currentElement);
    currentRecursionStack.pop();
  }

}
