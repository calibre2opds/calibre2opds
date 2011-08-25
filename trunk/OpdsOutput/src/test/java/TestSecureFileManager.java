import static org.junit.Assert.*;

import org.junit.Test;

import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;


public class TestSecureFileManager {

  @Test
  public void testEncode() {
    String s1 = SecureFileManager.INSTANCE.encode("toto");
    String s2 = SecureFileManager.INSTANCE.encode("toto");
    assertTrue(s1.equals(s2));
  }

}
