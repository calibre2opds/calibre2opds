package com.gmail.dpierron.calibre.opds;
/**
 *  Class for defining a new set of levels based on a tag.
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.RootTreeNode;
import com.gmail.dpierron.tools.TreeNode;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TagTreeSubCatalog extends TagsSubCatalog {
  private final static Logger logger = Logger.getLogger(TagTreeSubCatalog.class);

  public TagTreeSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType("tagtree");    // Not sure this will ever be used!
  }

  public TagTreeSubCatalog(List<Book> books) {
    super(books);
    setCatalogType("tagtree");    // Not sure this will ever be used!
  }

  private Element getLevelOfTreeNode(Breadcrumbs pBreadcrumbs, TreeNode level, int from) throws IOException {

    boolean inSubDir = ((getCatalogLevel().length() > 0) || (from != 0));
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int itemsCount = level.getChildren().size();

    String filename = pBreadcrumbs.getFilename() + Constants.TYPE_SEPARATOR + Constants.TAGS_TYPE + Constants.TYPE_SEPARATOR + level.getGuid();
    if (from > 0) {
      filename = filename + "_" + pageNumber;
    }
    filename = filename + Constants.XML_EXTENSION;
    logger.debug("getLevelOfTreeNode,int: generating " + filename);

    boolean onRoot = (level.isRoot());

    String title = (onRoot ? Localization.Main.getText("tags.title") : level.getId());
    String urn = Constants.INITIAL_URN_PREFIX + getCatalogType() + Constants.URN_SEPARATOR + level.getGuid();

    String summary = "";
    if (onRoot) {
      List<Tag> tags = getTags();
      if (tags.size() > 1)
        summary = Localization.Main.getText("tags.categorized", tags.size());
      else if (tags.size() == 1)
        summary = Localization.Main.getText("tags.categorized.single");
    } else {
      // try and list the items to make the summary
      summary = Summarizer.INSTANCE.summarizeTagLevels(level.getChildren());
    }
    int maxPages = Summarizer.INSTANCE.getPageNumber(itemsCount);

    String urlExt = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    List<Element> result = new LinkedList<Element>();
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

    for (int i = from; i < itemsCount; i++) {
      if ((i - from) >= maxBeforePaginate) {
        Element nextLink = getLevelOfTreeNode(pBreadcrumbs, level, i)/*.getFirstElement()*/;
        result.add(0, nextLink);
        break;
      } else {
        TreeNode childLevel = level.getChildren().get(i);
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        Element entry = getLevelOfTreeNode(breadcrumbs, childLevel)/*.getFirstElement()*/;
        if (entry != null)
          result.add(entry);
      }
    }

    feed.addContent(result);
    createFilesFromElement(feed,filename, HtmlManager.FeedType.Catalog);

    Element entry;
    String urlInItsSubfolder = catalogManager.getCatalogFileUrl(filename, inSubDir);
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages) {titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);} else {
        titleNext = Localization.Main.getText("title.lastpage");
      }

      entry = FeedHelper.getNextLink(urlExt, titleNext);
    } else {
      if (title.equals("Science Fiction")) {
        int x = 1;
      }
      if (logger.isTraceEnabled()) {logger.trace("getLevelOfTreeNode:  Breadcrumbs=" + pBreadcrumbs.toString());}
      entry = FeedHelper.getCatalogEntry(title, urn, urlInItsSubfolder, summary,
          useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS);
    }
    return entry;
  }

  /**
   *
   * @param pBreadcrumbs
   * @param level
   * @return
   * @throws IOException
   */
  private Element getLevelOfTreeNode(Breadcrumbs pBreadcrumbs, TreeNode level) throws IOException {
    logger.debug("getLevelOfTreeNode:" + level);
    if (Helper.isNullOrEmpty(level.getChildren())) {
      // it's a leaf, consisting of a single tag : make a list of books
      logger.debug("it's a leaf, consisting of a single tag : make a list of books");
      Tag tag = (Tag) level.getData();
      String urn = Constants.INITIAL_URN_PREFIX + getCatalogType();
      Element entry = getTag(pBreadcrumbs, tag, urn, level.getId());
      TrookSpecificSearchDatabaseManager.INSTANCE.addTag(tag, entry);
      return entry;
    } else {
      logger.debug("calling getLevelOfTreeNode,int");
      return getLevelOfTreeNode(pBreadcrumbs, level, 0);
    }
  }

  /**
   *
   * @param pBreadcrumbs
   * @return
   * @throws IOException
   */
  @Override
  Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    TreeNode root = getTreeOfTags(getTags());
    logger.debug("_getEntry:" + pBreadcrumbs.toString());
    return getLevelOfTreeNode(pBreadcrumbs, root);
  }

  /**
   *
   * @param tags
   * @return
   */
  private TreeNode getTreeOfTags(List<Tag> tags) {
    // compute the tree
    logger.debug("compute the tree of tags");
    TreeNode root = new RootTreeNode();
    for (Tag tag : tags) {
      String[] partsOfTag = tag.getPartsOfTag(currentProfile.getSplitTagsOn());
      TreeNode currentPositionInTree = root;
      for (int i = 0; i < partsOfTag.length; i++) {
        String partOfTag = partsOfTag[i];
        TreeNode nextPositionInTree = currentPositionInTree.getChildWithId(partOfTag);
        if (nextPositionInTree == null) {
          nextPositionInTree = new TreeNode(partOfTag);
          currentPositionInTree.addChild(nextPositionInTree);
          currentPositionInTree = nextPositionInTree;
        } else
          currentPositionInTree = nextPositionInTree;
      }
      currentPositionInTree.setData(tag);
    }
    // browse the tree, removing unneeded levels (single childs up to the leafs)
    logger.debug("remove unneeded levels");
    removeUnNeededLevelsInTree(root, null);

    return root;
  }

  /**
   *
   * @param node
   * @param removedParent
   * @return
   */
  private TreeNode removeUnNeededLevelsInTree(TreeNode node, TreeNode removedParent) {
    if (removedParent != null)
      node.setId(removedParent.getId() + currentProfile.getSplitTagsOn() + node.getId());

    if (node.getData() != null) {
      // this is a leaf
      return node;
    }

    List<TreeNode> newChildren = new LinkedList<TreeNode>();
    for (TreeNode childNode : node.getChildren()) {
      if (childNode.getData() == null && childNode.getChildren().size() <= 1) {
        if (childNode.getChildren().size() == 0) {
          // useless node
        } else {
          // useless level
          TreeNode newChild = removeUnNeededLevelsInTree(childNode.getChildren().get(0), childNode);
          if (newChild != null) {
            newChild.setParent(node);
            newChildren.add(newChild);
          }
        }
      } else {
        newChildren.add(removeUnNeededLevelsInTree(childNode, null));
      }
    }
    node.setChildren(newChildren);
    return node;
  }

}
