import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;


public class TestTrookSpecificDatabase {
  @Test
  public void testCreateDb() throws IOException, SQLException {
    File tempFile = File.createTempFile("testdb", ".db");
    TrookSpecificSearchDatabaseManager.setDatabaseFile(tempFile);
    assertTrue(TrookSpecificSearchDatabaseManager.databaseExists());
    PreparedStatement statement =
        TrookSpecificSearchDatabaseManager.getConnection().prepareStatement("select count(*) from KEYWORDS;");
    ResultSet set = statement.executeQuery();
    assertTrue(set.getInt(1) == 0);
  }
}
