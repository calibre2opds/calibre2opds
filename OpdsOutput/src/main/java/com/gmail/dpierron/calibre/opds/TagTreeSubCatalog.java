package com.gmail.dpierron.calibre.opds;
/**
 *  Class for defining a new tree set of levels based on a tag,
 *  with tags possibly split by a defined character.  This is
 *  a way f implementing what are known as hierarchical tags
 *  in Calibre.
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
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

  // CONSTRUCTOR(S)

  public TagTreeSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.TAGTREE_TYPE);
  }

  public TagTreeSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.TAGTREE_TYPE);
  }

  //  METHODS

  /**
   * Generate a tag tree for the current level
   *
   * The actual tag associated with a node is stored
   * as data information
   *
   * @param pBreadcrumbs
   * @return
   * @throws IOException
   */
  @Override
  Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    Element result;
    if (logger.isDebugEnabled()) logger.debug("getCatalog: pBreadcrumbs=" + pBreadcrumbs.toString() + ", inSubDir=" + inSubDir);
    String splitTagsOn = currentProfile.getSplitTagsOn();
    assert Helper.isNotNullOrEmpty(splitTagsOn);
    TreeNode root = new RootTreeNode();
    // Work through the tags creating the tree
    if (logger.isTraceEnabled()) logger.trace("generate initial tree");
    for (Tag tag : getTags()) {
      String[] partsOfTag = tag.getPartsOfTag(splitTagsOn);
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
      // Mark the tag this node uses
      currentPositionInTree.setData(tag);
    }
    // browse the tree, removing unneeded levels (single childs up to the leafs)
    if (logger.isTraceEnabled()) logger.trace("remove unneeded levels");
    removeUnNeededLevelsInTree(root, null);
    // Now get the resulting page set
    result = getLevelOfTreeNode(pBreadcrumbs, root);
    if (logger.isDebugEnabled()) logger.debug("getCatalog: exit (pBreadcrumbs=" + pBreadcrumbs.toString() + ")");
    return result;
  }

  /**
   * Trim un-needed nodes from the tree.
   *
   * We assume that any node that only has a single
   * child can effectively have the child collapsed
   * into the parent node. This will stop us generating
   * a series of pages that only have a single entry.
   *
   * NOTE:  It is written as a free-standing routine so it cn be called recursively.
   *
   * @param node
   * @param removedParent
   * @return
   */
  private TreeNode removeUnNeededLevelsInTree(TreeNode node, TreeNode removedParent) {
    // if (logger.isTraceEnabled()) logger.trace("removeUnNeededLevel: node=" + node + ", removedParent=" + removedParent);
    if (removedParent != null) {
      node.setId(removedParent.getId() + currentProfile.getSplitTagsOn() + node.getId());
    }
    if (node.getData() != null) {
      // this is a leaf
      return node;
    }

    List<TreeNode> newChildren = new LinkedList<TreeNode>();
    for (TreeNode childNode : node.getChildren()) {
      if (childNode.getData() == null && childNode.getChildren().size() <= 1) {
        if (childNode.getChildren().size() == 0) {
          // useless node
          // TODO:  ITIMPI:  Feel there should be something done here if this condition can really ever occur
          int dummy = 1;        // TODO See if we really ever get here!
        } else {
          // useless level so remove it
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

  /**
   * Initial entry point to creating a tree list of tags
   *
   * @param pBreadcrumbs
   * @param level
   * @return
   * @throws IOException
   */
  private Element getLevelOfTreeNode(Breadcrumbs pBreadcrumbs, TreeNode level) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("getLevelOfTreeNode: pBreadcrumbs=" + pBreadcrumbs + ", level=" + level);
    Element result;
    if (Helper.isNullOrEmpty(level.getChildren())) {
      Tag tag = (Tag) level.getData();
      if (tag == null) {
        if (logger.isDebugEnabled()) logger.debug("getLevelOfTreeNode: Exinull (Appears to be an empty level!)");
        return null;
      }
      // it's a leaf, consisting of a single tag : make a list of books
      if (logger.isTraceEnabled()) logger.trace("getLevelOfTreeNode: it's a leaf, consisting of a single tag : make a list of books");
      String urn = Constants.INITIAL_URN_PREFIX + getCatalogType()+ level.getGuid();
      result = getTagEntry(pBreadcrumbs, tag, urn, level.getId());
      TrookSpecificSearchDatabaseManager.INSTANCE.addTag(tag, result);
    } else {
      result = getLevelOfTreeNode(pBreadcrumbs, level, 0);
    }
    if (logger.isDebugEnabled()) logger.debug("getLevelOfTreeNode: Exit level " + level);
    return result;
  }


  /**
   * Get the psgrd of entries for a given level in the tree.
   *
   * @param pBreadcrumbs
   * @param level
   * @param from
   * @return
   * @throws IOException
   */
  private Element getLevelOfTreeNode(Breadcrumbs pBreadcrumbs, TreeNode level, int from) throws IOException {

    if (logger.isDebugEnabled()) logger.debug("getLevelOfTreeNode: pBreadcrumbs=" + pBreadcrumbs + ", level=" + level + ", from=" + from);

    boolean inSubDir = ((getCatalogLevel().length() > 0) || (from != 0) || pBreadcrumbs.size() > 1);
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int itemsCount = level.getChildren().size();

    String filename = getCatalogBaseFolderFileName()
//                    // TODO:  Get tag id as part of name to help with tracing source
                      + Constants.TYPE_SEPARATOR + encryptString(level.toString())
                      + Constants.PAGE_DELIM + pageNumber;
    logger.debug("getLevelOfTreeNode,int: generating " + filename);

    boolean onRoot = (level.isRoot());

    // TODO Might want to make the title include all 'parts' ?
    String title = (onRoot ? Localization.Main.getText("tags.title") : level.getId());
    String urn = Constants.INITIAL_URN_PREFIX + getCatalogType() + Constants.URN_SEPARATOR + encryptString(pBreadcrumbs.toString());
    String urlExt = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);

    String summary = "";
    if (onRoot) {
      int tagsSize = getTags().size();
      if (tagsSize > 1)
        summary = Localization.Main.getText("tags.categorized", tagsSize);
      else if (tagsSize == 1)
        summary = Localization.Main.getText("tags.categorized.single");
    } else {
      // try and list the items to make the summary
      summary = Summarizer.INSTANCE.summarizeTagLevels(level.getChildren());
    }
    int maxPages = Summarizer.INSTANCE.getPageNumber(itemsCount);

    List<Element> result = new LinkedList<Element>();
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /*inSubDir*/);

    for (int i = from; i < itemsCount; i++) {
      if ((i - from) >= maxBeforePaginate) {
        Element nextLink = getLevelOfTreeNode(pBreadcrumbs,
                                            level,
                                            i);
        result.add(0, nextLink);
        break;
      } else {
        TreeNode childLevel = level.getChildren().get(i);
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        Element entry = getLevelOfTreeNode(breadcrumbs,
                                           childLevel);
        if (entry != null)
          result.add(entry);
      }
    }

    if (logger.isTraceEnabled()) logger.trace("getLevelOfTreeNode: add entry to feed");
    feed.addContent(result);


    Element entry;
    String urlInItsSubfolder = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    entry = createPaginateLinks(feed, urlExt, pageNumber, maxPages);
    createFilesFromElement(feed,filename, HtmlManager.FeedType.Catalog);
    if (from == 0) {
      if (logger.isTraceEnabled()) {logger.trace("getLevelOfTreeNode:  Breadcrumbs=" + pBreadcrumbs.toString());}
      entry = FeedHelper.getCatalogEntry(title,
                                         urn,
                                         urlInItsSubfolder,
                                         summary,
                                         useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS);
    }
    if (logger.isDebugEnabled()) logger.debug("getLevelOfTreeNode: Exit level " + level + ", from=" + from);
    return entry;
  }
}
