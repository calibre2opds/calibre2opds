import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.opds.JDOM;
import com.gmail.dpierron.tools.Helper;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

public class TestJdom {

  @Test
  public void testconvertHtmlToXhtml() throws IOException {
    // read the test XHTML document
    InputStream is = getClass().getResourceAsStream("/testHtmlComment.html");
    String xml = Helper.readTextFile(is);
    List<Element> elements = JDOM.INSTANCE.convertBookCommentToXhtml(xml);
    Assert.assertTrue(elements.size() == 1);
  }

//  @Test
  // no markdown yet
  public void testconvertMarkdownToXhtml() throws IOException {
    // read the test markdown document
    InputStream is = getClass().getResourceAsStream("/testMarkdownComment.txt");
    String text = Helper.readTextFile(is);
    List<Element> elements = JDOM.INSTANCE.convertBookCommentToXhtml(text);
    Assert.assertTrue(elements.size() == 1);
    StringWriter sw = new StringWriter();
    JDOM.INSTANCE.getOutputter().output(elements.get(0), sw);
    String xml = sw.getBuffer().toString();
    Assert.assertFalse(xml.contains("###"));
  }

  @Test
  public void testconvertTextToXhtml() throws IOException {
    // read the test text document
    InputStream is = getClass().getResourceAsStream("/testTextComment.txt");
    String xml = Helper.readTextFile(is);
    List<Element> elements = JDOM.INSTANCE.convertBookCommentToXhtml(xml);
    for (Element element : elements) {
      System.out.println(element.getText());
    }
    Assert.assertTrue(elements.size() == 61);
  }

  //@Test
  public void test() {
    DatabaseManager.INSTANCE.initConnection(new File("/Users/david/Downloads"));
    Book book = DataModel.INSTANCE.getMapOfBooks().get("12828");
    System.out.println(book.getComment());
  }
}
