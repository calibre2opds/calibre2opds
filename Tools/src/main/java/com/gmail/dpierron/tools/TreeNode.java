package com.gmail.dpierron.tools;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TreeNode {
  private Object data;
  private String id;
  private TreeNode parent;
  private List<TreeNode> children;

  public TreeNode(String id) {
    this(id, null);
  }

  public TreeNode(String id, Object data) {
    super();
    this.data = data;
    this.id = id;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public boolean isRoot() {
    return this instanceof RootTreeNode;
  }

  public TreeNode getParent() {
    return parent;
  }

  public void setParent(TreeNode parent) {
    this.parent = parent;
  }

  public List<TreeNode> getChildren() {
    if (children == null)
      children = new LinkedList<TreeNode>();
    return children;
  }

  public void setChildren(List<TreeNode> newChildren) {
    this.children = newChildren;
  }

  public TreeNode getChildWithId(String id) {
    for (TreeNode childNode : getChildren()) {
      if (childNode.getId().equals(id))
        return childNode;
    }
    return null;
  }

  public void addChild(TreeNode child) {
    if (child == null)
      return;
    getChildren().add(child);
    child.setParent(this);
  }

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getIdToRoot() {
    return getIdToRoot("_");
  }

  public String getIdToRoot(String separator) {
    List<String> ids = new LinkedList<String>();
    TreeNode node = this;
    while (node.getParent() != null) {
      ids.add(node.getId());
      node = node.getParent();
    }
    Collections.reverse(ids);
    return Helper.concatenateList(separator, ids);
  }

  public String getGuid() {
    String guid = getIdToRoot();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < guid.length(); i++) {
      char c = guid.charAt(i);
      if (Character.isLetterOrDigit(c))
        sb.append(c);
      else
        sb.append('_');
    }
    return sb.toString();
  }

  public String toString() {
    String base = getIdToRoot("/");
    if (getData() == null)
      return base;
    else
      return base + " (" + getData() + ")";
  }
}
